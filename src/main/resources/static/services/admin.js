"use strict";

const API_BASE = "http://localhost:8181";

let currentMenuItems = []; // cache del menú actual
let currentOrders = [];    // cache de pedidos actuales

document.addEventListener("DOMContentLoaded", () => {
  const titleEl = document.getElementById("admin-title");
  const viewEl = document.getElementById("admin-view");

  const btnPedidos   = document.getElementById("btn-pedidos");
  const btnMenuDia   = document.getElementById("btn-menu-dia");
  const btnAdminUser = document.getElementById("btn-admin-user");

  const buttons = [btnPedidos, btnMenuDia, btnAdminUser];

  function setActive(btn) {
    buttons.forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");
  }

  // ---------- VISTA: PEDIDOS PENDIENTES ----------
  function showPedidosView() {
    titleEl.textContent = "Pedidos pendientes";
    viewEl.innerHTML = `
      <div class="admin-card-header">
        <div>
          <h2>Pedidos pendientes</h2>
          <p class="small-text">
            Aquí puedes ver los pedidos pendientes y actualizar su estado.
          </p>
        </div>
      </div>

      <div id="orders-list">
        <p class="small-text">Cargando pedidos...</p>
      </div>

      <div id="order-modal-container" class="hidden"></div>
    `;

    const listEl = document.getElementById("orders-list");
    loadOrders(listEl);
  }

  // ---------- CARGAR LISTA DE PEDIDOS PENDIENTES ----------
  async function loadOrders(container) {
    try {
      // Endpoint sugerido: GET /orders/pending
      const res = await fetch(`${API_BASE}/orders/pending`);

      if (!res.ok) {
        container.innerHTML = "<p>No se pudieron cargar los pedidos.</p>";
        return;
      }

      const orders = await res.json();
      currentOrders = orders || [];

      if (!orders || orders.length === 0) {
        container.innerHTML =
          "<p class='small-text'>No hay pedidos pendientes en este momento.</p>";
        return;
      }

      const rows = orders
        .map((order) => {
          const id = order.id ?? "";

          const userLabel =
            (order.user &&
              (order.user.username ||
                order.user.nombre ||
                order.user.email)) ||
            (order.user && `ID ${order.user.id}`) ||
            "-";

          const aulaLabel =
            (order.aula &&
              (order.aula.codigo ||
                order.aula.nombre)) ||
            (order.aula && `ID ${order.aula.id}`) ||
            "-";

          const fecha = order.createdAt
            ? order.createdAt.toString().replace("T", " ").substring(0, 16)
            : "-";

          const total = order.total ?? 0;
          const estado = order.estado || "PENDIENTE";
          const estadoPago = order.estadoPago || "PENDIENTE";

          // ====== NUEVA LÓGICA DEL SELECT DE ESTADO ======
          // Solo permitir cambiar desde PENDIENTE → EN_PREPARACION o CANCELADO
          let optionsHtml = "";

          if (estado === "PENDIENTE") {
            // Placeholder con el estado actual (no seleccionable)
            optionsHtml += `
              <option value="${estado}" selected disabled hidden>
                ${estado.replace("_", " ")}
              </option>
            `;

            ["EN_PREPARACION", "CANCELADO"].forEach((st) => {
              optionsHtml += `
                <option value="${st}">
                  ${st.replace("_", " ")}
                </option>
              `;
            });
          } else {
            // Si ya no está pendiente, solo mostrar el estado actual
            optionsHtml += `
              <option value="${estado}" selected>
                ${estado.replace("_", " ")}
              </option>
            `;
          }
          // ================================================

          return `
            <tr data-id="${id}">
              <td>${id}</td>
              <td>${userLabel}</td>
              <td>${aulaLabel}</td>
              <td>${fecha}</td>
              <td>${total} Bs</td>
              <td>${estado}</td>
              <td>${estadoPago}</td>
              <td>
                <div class="menu-actions">
                  <button
                    type="button"
                    class="btn-ghost btn-sm js-order-info"
                    data-id="${id}"
                  >
                    Ver detalle
                  </button>
                  <select
                    class="js-order-status btn-sm"
                    data-id="${id}"
                    ${estado !== "PENDIENTE" ? "disabled" : ""}
                  >
                    ${optionsHtml}
                  </select>
                </div>
              </td>
            </tr>
          `;
        })
        .join("");

      container.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Usuario</th>
              <th>Aula</th>
              <th>Creado</th>
              <th>Total</th>
              <th>Estado</th>
              <th>Pago</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${rows}
          </tbody>
        </table>
      `;

      attachOrderRowHandlers(container);
    } catch (err) {
      console.error(err);
      container.innerHTML = "<p>Error de conexión al cargar pedidos.</p>";
    }
  }

  // Añadir handlers a botones de pedidos
  function attachOrderRowHandlers(container) {
    const modalContainer = document.getElementById("order-modal-container");

    // Botón VER DETALLE
    container.querySelectorAll(".js-order-info").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = Number(btn.dataset.id);
        const order = currentOrders.find((o) => o.id === id);
        if (!order) return;
        renderOrderModal(modalContainer, order);
      });
    });

    // Selector de ESTADO
    container.querySelectorAll(".js-order-status").forEach((sel) => {
      sel.addEventListener("change", () => {
        const id = Number(sel.dataset.id);
        const newEstado = sel.value;
        updateOrderStatus(id, newEstado, sel);
      });
    });
  }

  // ---------- MODAL DETALLE DE PEDIDO ----------
  function renderOrderModal(container, order) {
    const userLabel =
      (order.user &&
        (order.user.username ||
          order.user.nombre ||
          order.user.email)) ||
      (order.user && `ID ${order.user.id}`) ||
      "-";

    const aulaLabel =
      (order.aula &&
        (order.aula.codigo ||
          order.aula.nombre)) ||
      (order.aula && `ID ${order.aula.id}`) ||
      "-";

    const fecha = order.createdAt
      ? order.createdAt.toString().replace("T", " ").substring(0, 16)
      : "-";

    const items = order.items || [];

    const rows = items
      .map((it) => {
        const prod =
          (it.menuItem &&
            (it.menuItem.nombreProducto ||
              it.menuItem.nombre)) ||
          (it.menuItem && `ID ${it.menuItem.id}`) ||
          "-";
        const cant = it.cantidad ?? 0;
        const precio = it.precioItem ?? 0;
        const subtotal = it.subtotal ?? 0;

        return `
          <tr>
            <td>${prod}</td>
            <td>${cant}</td>
            <td>${precio} Bs</td>
            <td>${subtotal} Bs</td>
          </tr>
        `;
      })
      .join("");

    container.innerHTML = `
      <div class="modal-backdrop">
        <div class="modal">
          <div class="modal-content">
            <h2>Pedido #${order.id}</h2>
            <p class="small-text">
              Usuario: <strong>${userLabel}</strong><br/>
              Aula: <strong>${aulaLabel}</strong><br/>
              Creado: <strong>${fecha}</strong><br/>
              Estado: <strong>${order.estado}</strong> |
              Pago: <strong>${order.estadoPago}</strong>
            </p>

            <h3>Items</h3>
            ${
              items.length === 0
                ? "<p class='small-text'>Este pedido no tiene items.</p>"
                : `
                  <table>
                    <thead>
                      <tr>
                        <th>Producto</th>
                        <th>Cant.</th>
                        <th>Precio</th>
                        <th>Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      ${rows}
                    </tbody>
                  </table>
                `
            }

            <p style="margin-top: 12px;">
              Total: <strong>${order.total ?? 0} Bs</strong>
            </p>

            <div class="modal-actions">
              <button type="button" id="btn-close-order-modal" class="btn-secondary">
                Cerrar
              </button>
            </div>
          </div>
        </div>
      </div>
    `;

    container.classList.remove("hidden");

    const btnClose = document.getElementById("btn-close-order-modal");
    btnClose.addEventListener("click", () => {
      container.classList.add("hidden");
      container.innerHTML = "";
    });
  }

  // ---------- ACTUALIZAR ESTADO DEL PEDIDO ----------
  async function updateOrderStatus(orderId, newEstado, selectEl) {
    try {
      const payload = { estado: newEstado };

      const res = await fetch(`${API_BASE}/orders/${orderId}/estado`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        console.error("Error al actualizar estado de pedido:", res.status, txt);
        alert("No se pudo actualizar el estado del pedido.");
        return;
      }

      const updated = await res.json();

      // actualizar cache
      const idx = currentOrders.findIndex((o) => o.id === orderId);
      if (idx !== -1) {
        currentOrders[idx] = updated;
      }
    } catch (err) {
      console.error(err);
      alert("Error de conexión al actualizar el estado del pedido.");
    }
  }

  // ---------- VISTA: MENÚ DEL DÍA ----------
  function showMenuDiaView() {
    titleEl.textContent = "Crear menú del día";
    viewEl.innerHTML = `
      <div class="admin-card-header">
        <div>
          <h2>Productos del menú del día</h2>
          <p class="small-text">
            Crea productos (con imagen JPG, stock y descripción) y se listarán como parte del menú del día.
          </p>
        </div>
        <button id="btn-open-product-modal" class="btn-primary">
          Crear producto
        </button>
      </div>

      <h3>Productos en el menú del día</h3>
      <div id="menu-list">
        <p class="small-text">Cargando productos...</p>
      </div>

      <div id="product-modal-container" class="hidden"></div>
    `;

    const listEl = document.getElementById("menu-list");
    const modalContainer = document.getElementById("product-modal-container");
    const openModalBtn = document.getElementById("btn-open-product-modal");

    // Abrir modal en modo "nuevo"
    openModalBtn.addEventListener("click", () => {
      renderProductModal(modalContainer, listEl, null);
    });

    loadMenuItems(listEl);
  }

  // ---------- MODAL NUEVO / EDITAR PRODUCTO ----------
  function renderProductModal(container, listEl, existingItem) {
    const isEdit = !!existingItem;

    container.innerHTML = `
      <div class="modal-backdrop">
        <div class="modal">
          <div class="modal-content">
            <h2>${isEdit ? "Editar producto" : "Nuevo producto"}</h2>
            <form id="product-form">
              <div class="form-grid">
                <input
                  type="text"
                  name="nombre"
                  placeholder="Nombre del producto"
                  required
                />
                <input
                  type="number"
                  step="0.01"
                  name="precio"
                  placeholder="Precio"
                  required
                />
                <input
                  type="number"
                  min="0"
                  step="1"
                  name="stock"
                  placeholder="Stock disponible"
                  required
                />
                <textarea
                  name="descripcion"
                  placeholder="Descripción (opcional)"
                ></textarea>
                ${
                  isEdit
                    ? ""
                    : `
                <input
                  type="file"
                  name="imagen"
                  accept=".jpg,.jpeg"
                  required
                />`
                }
              </div>
              <div class="modal-actions">
                <button
                  type="button"
                  id="btn-cancel-product"
                  class="btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" class="btn-primary">
                  ${isEdit ? "Guardar cambios" : "Guardar producto"}
                </button>
              </div>
            </form>
            <p class="small-text">
              ${
                isEdit
                  ? "Si no cambias nada, se mantiene la misma imagen."
                  : "Solo se permiten imágenes JPG/JPEG. Se guardarán en el servidor y se mostrarán en el menú."
              }
            </p>
          </div>
        </div>
      </div>
    `;

    container.classList.remove("hidden");

    const form = document.getElementById("product-form");
    const btnCancel = document.getElementById("btn-cancel-product");

    // Prefill en modo edición
    if (isEdit) {
      form.nombre.value =
        existingItem.nombreProducto || existingItem.nombre || "";
      form.precio.value = existingItem.precio ?? "";
      form.stock.value = existingItem.stock ?? 0;
      form.descripcion.value = existingItem.descripcion || "";
    }

    btnCancel.addEventListener("click", () => {
      container.classList.add("hidden");
      container.innerHTML = "";
    });

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const fd = new FormData(form);
      const nombre = (fd.get("nombre") || "").toString().trim();
      const precio = parseFloat(fd.get("precio") || "0");
      const stock = parseInt(fd.get("stock") || "0", 10);
      const descripcion = (fd.get("descripcion") || "").toString().trim();

      if (!nombre) {
        alert("El nombre es obligatorio.");
        return;
      }
      if (!Number.isFinite(precio) || precio <= 0) {
        alert("El precio debe ser mayor que 0.");
        return;
      }
      if (!Number.isInteger(stock) || stock < 0) {
        alert("El stock debe ser un número entero mayor o igual a 0.");
        return;
      }

      try {
        if (isEdit) {
          // ----- EDITAR (PUT /menu/{id}) -----
          const payload = {
            id: existingItem.id,
            nombreProducto: nombre,
            precio,
            descripcion,
            stock,
            imageUrl: existingItem.imageUrl || null,
            estado: existingItem.estado || "INACTIVO",
          };

          const res = await fetch(`${API_BASE}/menu/${existingItem.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });

          if (!res.ok) {
            const txt = await res.text().catch(() => "");
            console.error("Error al editar producto:", res.status, txt);
            alert("No se pudo editar el producto.");
            return;
          }
        } else {
          // ----- CREAR (POST /menu/with-image) -----
          const file = fd.get("imagen");
          if (!file || !file.name) {
            alert("Selecciona una imagen JPG.");
            return;
          }
          const ext = file.name.split(".").pop().toLowerCase();
          if (ext !== "jpg" && ext !== "jpeg") {
            alert("La imagen debe ser .jpg o .jpeg");
            return;
          }

          const toSend = new FormData();
          toSend.append("nombre", nombre);
          toSend.append("precio", precio.toString());
          toSend.append("stock", stock.toString());
          toSend.append("descripcion", descripcion);
          toSend.append("imagen", file);

          const res = await fetch(`${API_BASE}/menu/with-image`, {
            method: "POST",
            body: toSend,
          });

          if (!res.ok) {
            const txt = await res.text().catch(() => "");
            console.error("Error al crear producto:", res.status, txt);
            alert("No se pudo crear el producto de menú.");
            return;
          }
        }

        alert(isEdit ? "Producto actualizado." : "Producto creado correctamente.");
        container.classList.add("hidden");
        container.innerHTML = "";
        loadMenuItems(listEl);
      } catch (err) {
        console.error(err);
        alert("Error de conexión al guardar el producto.");
      }
    });
  }

  // ---------- CARGAR LISTA DE MENÚ + BOTONES ----------
  async function loadMenuItems(container) {
    try {
      const res = await fetch(`${API_BASE}/menu`);
      if (!res.ok) {
        container.innerHTML = "<p>No se pudieron cargar los productos.</p>";
        return;
      }

      const items = await res.json();
      currentMenuItems = items || [];

      if (!items || items.length === 0) {
        container.innerHTML =
          "<p class='small-text'>No hay productos cargados en el menú del día.</p>";
        return;
      }

      const rows = items
        .map((item) => {
          const id = item.id ?? "";
          const nombre = item.nombreProducto || item.nombre || `ID ${id}`;
          const precio =
            item.precio !== undefined && item.precio !== null
              ? item.precio
              : "-";
          const desc = item.descripcion || "";
          const stock =
            item.stock !== undefined && item.stock !== null
              ? item.stock
              : "-";
          const imgHtml = item.imageUrl
            ? `<img src="${item.imageUrl}" alt="${nombre}" class="menu-image" />`
            : "";

          const isActive =
            item.estado === "ACTIVO" ||
            item.estado === "Activo" ||
            item.estado === true;
          const checkedAttr = isActive ? "checked" : "";
          const labelEstado = isActive ? "Activo" : "Inactivo";

          return `
            <tr data-id="${id}">
              <td>${id}</td>
              <td>${nombre}</td>
              <td>${precio}</td>
              <td>${stock}</td>
              <td>${desc}</td>
              <td>
                <div class="menu-actions">
                  ${imgHtml}
                  <button
                    type="button"
                    class="btn-ghost btn-sm js-edit-product"
                    data-id="${id}"
                  >
                    Editar
                  </button>
                  <label class="estado-toggle">
                    <input
                      type="checkbox"
                      class="js-toggle-estado"
                      data-id="${id}"
                      ${checkedAttr}
                    />
                    <span>${labelEstado}</span>
                  </label>
                </div>
              </td>
            </tr>
          `;
        })
        .join("");

      container.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Producto</th>
              <th>Precio</th>
              <th>Stock</th>
              <th>Descripción</th>
              <th>Imagen / Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${rows}
          </tbody>
        </table>
      `;

      attachMenuRowHandlers(container);
    } catch (err) {
      console.error(err);
      container.innerHTML = "<p>Error de conexión al cargar el menú.</p>";
    }
  }

  // Añadir handlers a botones Editar y checkbox de estado
  function attachMenuRowHandlers(container) {
    const modalContainer = document.getElementById("product-modal-container");
    const listEl = document.getElementById("menu-list");

    // Botón EDITAR
    container.querySelectorAll(".js-edit-product").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = Number(btn.dataset.id);
        const item = currentMenuItems.find((m) => m.id === id);
        if (!item) return;
        renderProductModal(modalContainer, listEl, item);
      });
    });

    // Checkbox ESTADO
    container.querySelectorAll(".js-toggle-estado").forEach((chk) => {
      chk.addEventListener("change", async () => {
        const id = Number(chk.dataset.id);
        const item = currentMenuItems.find((m) => m.id === id);
        if (!item) return;

        const newEstado = chk.checked ? "ACTIVO" : "INACTIVO";

        const label = chk.closest(".estado-toggle")?.querySelector("span");
        if (label) {
          label.textContent = newEstado === "ACTIVO" ? "Activo" : "Inactivo";
        }

        // actualizar en memoria
        item.estado = newEstado;

        try {
          const payload = {
            id: item.id,
            nombreProducto: item.nombreProducto,
            precio: item.precio,
            descripcion: item.descripcion,
            stock: item.stock ?? 0,
            imageUrl: item.imageUrl || null,
            estado: newEstado,
          };

          const res = await fetch(`${API_BASE}/menu/${item.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });

          if (!res.ok) {
            console.error("Error al cambiar estado:", res.status);
            alert("No se pudo actualizar el estado del producto.");
          }
        } catch (err) {
          console.error(err);
          alert("Error de conexión al actualizar estado.");
        }
      });
    });
  }

  // ---------- VISTA: CREAR USUARIO ADMIN ----------
  function showAdminUserView() {
    titleEl.textContent = "Crear usuario administrador";
    viewEl.innerHTML = `
      <h2>Crear usuario administrador</h2>
      <p class="small-text">
        Aquí podrás registrar nuevos administradores. Se envían a la API /admins.
      </p>
      <form id="admin-form">
        <div class="form-grid">
          <input
            type="text"
            name="username"
            placeholder="Usuario administrador"
            required
          />
          <input
            type="password"
            name="password"
            placeholder="Contraseña"
            required
          />
        </div>
        <button type="submit" class="btn-primary">Crear administrador</button>
      </form>
    `;

    const form = document.getElementById("admin-form");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const data = new FormData(form);
      const payload = {
        username: (data.get("username") || "").toString().trim(),
        password: (data.get("password") || "").toString(),
      };

      if (!payload.username || !payload.password) {
        alert("Completa usuario y contraseña.");
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/admins`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        if (!res.ok) {
          alert("No se pudo crear el administrador.");
          return;
        }

        alert("Administrador creado correctamente.");
        form.reset();
      } catch (err) {
        console.error(err);
        alert("Error de conexión al crear administrador.");
      }
    });
  }

  // ---------- Navegación ----------
  btnPedidos.addEventListener("click", () => {
    setActive(btnPedidos);
    showPedidosView();
  });

  btnMenuDia.addEventListener("click", () => {
    setActive(btnMenuDia);
    showMenuDiaView();
  });

  btnAdminUser.addEventListener("click", () => {
    setActive(btnAdminUser);
    showAdminUserView();
  });

  // Vista inicial
  setActive(btnPedidos);
  showPedidosView();
});
