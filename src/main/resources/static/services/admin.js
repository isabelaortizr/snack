"use strict";

const API_BASE = "http://localhost:8181";

let currentMenuItems = []; // cache del menú actual

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
      <p>Aquí se mostrarán los pedidos pendientes para gestionar.</p>
    `;
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
      form.nombre.value = existingItem.nombreProducto || existingItem.nombre || "";
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
