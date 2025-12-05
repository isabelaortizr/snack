package com.upb.snack.dto;

public class UpdateOrderStatusRequest {

    // Nuevo estado del pedido: PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO, etc.
    private String estado;

    // Opcional: estado del pago (PENDIENTE, PAGADO, CANCELADO, etc.)
    private String estadoPago;

    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(String estado, String estadoPago) {
        this.estado = estado;
        this.estadoPago = estadoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequest{" +
                "estado='" + estado + '\'' +
                ", estadoPago='" + estadoPago + '\'' +
                '}';
    }
}
