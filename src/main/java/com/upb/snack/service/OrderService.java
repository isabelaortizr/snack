package com.upb.snack.service;

import com.upb.snack.dto.CreateOrderRequest;
import com.upb.snack.entity.Aula;
import com.upb.snack.entity.Menu;
import com.upb.snack.entity.Order;
import com.upb.snack.entity.OrderItem;
import com.upb.snack.entity.User;
import com.upb.snack.repository.AulaRepository;
import com.upb.snack.repository.MenuRepository;
import com.upb.snack.repository.OrderRepository;
import com.upb.snack.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AulaRepository aulaRepository;
    private final MenuRepository menuRepository;

    /**
     * Servicio que gestiona los códigos de verificación/descuento.
     * Debe exponer al menos:
     *  - boolean isValid(String code)
     *  - void markCodeAsUsed(String code)
     */
    private final VerificationCodeService verificationCodeService;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            AulaRepository aulaRepository,
            MenuRepository menuRepository,
            VerificationCodeService verificationCodeService
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.aulaRepository = aulaRepository;
        this.menuRepository = menuRepository;
        this.verificationCodeService = verificationCodeService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Devuelve todos los pedidos con un estado concreto, ordenados por fecha (asc).
     * Ej: "PENDIENTE" para la vista de pedidos pendientes del admin.
     */
    public List<Order> getOrdersByEstado(String estado) {
        String safeEstado = Objects.requireNonNull(estado, "estado es obligatorio");
        return orderRepository.findByEstadoIgnoreCaseOrderByCreatedAtAsc(safeEstado);
    }

    /**
     * Crea un nuevo pedido en estado PENDIENTE a partir del carrito.
     * Si se envía un discountCode válido, aplica un 20% de descuento
     * sobre el subtotal del pedido.
     */
    public Order createOrder(CreateOrderRequest request) {
        CreateOrderRequest safeRequest =
                Objects.requireNonNull(request, "La orden es obligatoria");

        Long userId = Objects.requireNonNull(
                safeRequest.getUserId(),
                "userId es obligatorio"
        );
        Long aulaId = Objects.requireNonNull(
                safeRequest.getAulaId(),
                "aulaId es obligatorio"
        );

        // 1. Buscar usuario y aula
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));

        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new EntityNotFoundException("Aula no encontrada: " + aulaId));

        if (safeRequest.getItems() == null || safeRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un item.");
        }

        // 2. Crear Order base
        Order order = new Order();
        order.setUser(user);
        order.setAula(aula);
        order.setEstado("PENDIENTE");
        order.setEstadoPago("PENDIENTE");
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        int subtotal = 0;

        // 3. Crear OrderItem por cada item del request y calcular subtotal
        for (CreateOrderRequest.Item itemReq : safeRequest.getItems()) {
            Long menuItemId = Objects.requireNonNull(
                    itemReq.getMenuItemId(),
                    "menuItemId es obligatorio"
            );
            Integer cantidad = Objects.requireNonNull(
                    itemReq.getCantidad(),
                    "cantidad es obligatoria"
            );

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }

            Menu menu = menuRepository.findById(menuItemId)
                    .orElseThrow(() -> new EntityNotFoundException("Menu no encontrado: " + menuItemId));

            // getPrecio() es Double/double → lo convertimos a int (Bs enteros)
            double rawPrecio = menu.getPrecio() == null ? 0.0 : menu.getPrecio();
            int precioItem = (int) Math.round(rawPrecio);

            int itemSubtotal = precioItem * cantidad;

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menu);
            orderItem.setOrder(order);
            orderItem.setCantidad(cantidad);
            orderItem.setPrecioItem(precioItem);
            orderItem.setSubtotal(itemSubtotal);

            items.add(orderItem);
            subtotal += itemSubtotal;
        }

        // 4. Aplicar descuento si se envió un código válido
        String discountCode = safeRequest.getDiscountCode();
        int discountAmount = 0;

        if (discountCode != null && !discountCode.isBlank()) {
            boolean valido = verificationCodeService.isValid(discountCode);

            if (!valido) {
                throw new IllegalArgumentException("Código de descuento inválido o expirado.");
            }

            // 20% de descuento sobre el subtotal
            discountAmount = (int) Math.round(subtotal * 0.20);

            // Marcamos el código como usado para que no se reutilice
            verificationCodeService.markCodeAsUsed(discountCode);

            // Si luego agregas campos en Order para guardar el código / monto,
            // aquí sería el lugar para setearlos.
            // order.setDiscountCode(discountCode);
            // order.setDiscountAmount(discountAmount);
        }

        int total = subtotal - discountAmount;
        if (total < 0) {
            total = 0;
        }

        order.setTotal(total);
        order.setItems(items);

        // 5. Guardar (cascade = ALL en items)
        return orderRepository.save(order);
    }

    /**
     * Devuelve el pedido pendiente más reciente del usuario (o null si no tiene).
     */
    public Order getCurrentOrderForUser(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId es obligatorio");

        return orderRepository
                .findFirstByUser_IdAndEstadoOrderByCreatedAtDesc(safeUserId, "PENDIENTE")
                .orElse(null);
    }

    /**
     * Cancela un pedido PENDIENTE del propio usuario.
     */
    public Order cancelOrder(Long orderId, Long userId) {
        Long safeOrderId = Objects.requireNonNull(orderId, "orderId es obligatorio");
        Long safeUserId = Objects.requireNonNull(userId, "userId es obligatorio");

        Order order = orderRepository.findById(safeOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + safeOrderId));

        if (order.getUser() == null || !safeUserId.equals(order.getUser().getId())) {
            throw new IllegalArgumentException("No puedes cancelar un pedido de otro usuario.");
        }

        if (!"PENDIENTE".equalsIgnoreCase(order.getEstado())) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos en estado PENDIENTE.");
        }

        order.setEstado("CANCELADO");
        order.setEstadoPago("CANCELADO");
        return orderRepository.save(order);
    }

    /**
     * Cancela automáticamente pedidos PENDIENTES más antiguos que maxPendingMinutes.
     */
    public int autoCancelOldPendingOrders(int maxPendingMinutes) {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(maxPendingMinutes);

        List<Order> pendientesViejos =
                orderRepository.findByEstadoAndCreatedAtBefore("PENDIENTE", limite);

        if (pendientesViejos.isEmpty()) {
            return 0;
        }

        for (Order o : pendientesViejos) {
            o.setEstado("CANCELADO");
            o.setEstadoPago("CANCELADO");
        }

        orderRepository.saveAll(pendientesViejos);
        return pendientesViejos.size();
    }

    /**
     * Actualiza el estado (y opcionalmente estadoPago) de un pedido.
     * Usado por el admin para mover PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO / CANCELADO.
     */
    public Order updateOrderStatus(Long orderId, String estado, String estadoPago) {
        Long safeOrderId = Objects.requireNonNull(orderId, "orderId es obligatorio");
        String safeEstado = Objects.requireNonNull(estado, "estado es obligatorio");

        Order order = orderRepository.findById(safeOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + orderId));

        order.setEstado(safeEstado);

        if (estadoPago != null && !estadoPago.isBlank()) {
            order.setEstadoPago(estadoPago);
        }

        return orderRepository.save(order);
    }
}
