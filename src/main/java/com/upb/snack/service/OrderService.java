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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AulaRepository aulaRepository;
    private final MenuRepository menuRepository;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            AulaRepository aulaRepository,
            MenuRepository menuRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.aulaRepository = aulaRepository;
        this.menuRepository = menuRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Crea un nuevo pedido en estado PENDIENTE a partir del carrito.
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

        // 2. Crear Order
        Order order = new Order();
        order.setUser(user);
        order.setAula(aula);
        order.setEstado("PENDIENTE");
        order.setEstadoPago("PENDIENTE");
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        int total = 0;

        // 3. Crear OrderItem por cada item del request
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

            int subtotal = precioItem * cantidad;

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menu);
            orderItem.setOrder(order);
            orderItem.setCantidad(cantidad);
            orderItem.setPrecioItem(precioItem);
            orderItem.setSubtotal(subtotal);

            items.add(orderItem);
            total += subtotal;
        }

        order.setTotal(total);
        order.setItems(items);

        // 4. Guardar (cascade = ALL en items)
        return orderRepository.save(order);
    }

    /**
     * Devuelve el pedido pendiente más reciente del usuario (o null si no tiene).
     */
    public Order getCurrentOrderForUser(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId es obligatorio");

        // Para no tocar el repository, usamos findAll() y filtramos en memoria
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser() != null && safeUserId.equals(o.getUser().getId()))
                .filter(o -> "PENDIENTE".equalsIgnoreCase(o.getEstado()))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .findFirst()
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

        // Este método sí requiere que lo tengas en OrderRepository:
        // List<Order> findByEstadoAndCreatedAtBefore(String estado, LocalDateTime before);
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
}
