"use strict";

const API_BASE = "http://localhost:8181";

document.addEventListener("DOMContentLoaded", () => {
  // Ya no hay panel lateral de carrito:
  const cartListEl = null;
  const cartTotalEl = null;

  const productListEl = document.getElementById("productList");
  const classroomInput = document.getElementById("classroom");
  const cartBadgeEl = document.getElementById("cartBadge");

  // Botón carrito del topbar
  const cartButton = document.getElementById("cartButton");

  // Modal de aula
  const classModal = document.getElementById("classModal");
  const buildingListEl = document.getElementById("modalBuildingList");
  const floorListEl = document.getElementById("modalFloorList");
  const modalSelectedLabel = document.getElementById("modalSelectedLabel");
  const confirmClassBtn = document.getElementById("confirmClassBtn");

  // Modal carrito
  const cartModal = document.getElementById("cartModal");
  const cartModalListEl = document.getElementById("cartModalList");
  const cartModalTotalEl = document.getElementById("cartModalTotal");
  const cartModalStatusEl = document.getElementById("cartModalStatus");
  const closeCartModalBtn = document.getElementById("closeCartModalBtn");
  const cartModalCheckoutBtn = document.getElementById("cartModalCheckoutBtn");

  let cart = [];

  // Aulas agrupadas: { "Edificio 1": { "1": [aulas piso1], "2": [...], ... }, ... }
  let aulasPorEdificio = {};
  let selectedBuilding = null;
  let selectedAula = null;

  // ---- helper para leer el usuario logueado (guardado en login.js) ----
  function getLoggedUser() {
    try {
      const raw = localStorage.getItem("snackUser");
      if (!raw) return null;
      return JSON.parse(raw);
    } catch (e) {
      console.error("Error leyendo snackUser de localStorage", e);
      return null;
    }
  }

  function setStatus(message, type = "info") {
    if (!cartModalStatusEl) return;
    cartModalStatusEl.hidden = !message;
    cartModalStatusEl.textContent = message || "";
    cartModalStatusEl.classList.remove("error", "success");
    if (type === "error") cartModalStatusEl.classList.add("error");
    if (type === "success") cartModalStatusEl.classList.add("success");
  }

  // ====== MODAL AULA ======

  async function initClassroomModal() {
    if (!classModal) return;

    classModal.hidden = false;
    if (confirmClassBtn) confirmClassBtn.disabled = true;
    selectedBuilding = null;
    selectedAula = null;
    updateSelectedLabel("Elige edificio, piso y aula.");

    try {
      const res = await fetch(`${API_BASE}/aulas`);
      if (!res.ok) {
        throw new Error("No se pudieron cargar las aulas");
      }
      const aulas = await res.json();

      const map = {};
      (aulas || []).forEach((a) => {
        const edificio = a.edificio || "Sin edificio";
        const piso = a.piso != null ? a.piso : 0;

        if (!map[edificio]) {
          map[edificio] = {};
        }
        if (!map[edificio][piso]) {
          map[edificio][piso] = [];
        }
        map[edificio][piso].push(a);
      });

      aulasPorEdificio = map;
      renderBuildings();
    } catch (err) {
      console.error(err);
      if (buildingListEl) {
        buildingListEl.innerHTML = "<p>No se pudieron cargar las aulas.</p>";
      }
      if (floorListEl) {
        floorListEl.innerHTML = "";
      }
    }
  }

  function renderBuildings() {
    if (!buildingListEl || !floorListEl) return;

    buildingListEl.innerHTML = "";
    floorListEl.innerHTML = "";

    const edificios = Object.keys(aulasPorEdificio);
    if (edificios.length === 0) {
      buildingListEl.innerHTML = "<p>No hay edificios configurados.</p>";
      return;
    }

    edificios.forEach((edificio) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "pill-btn";
      btn.textContent = edificio;

      btn.addEventListener("click", () => {
        selectedBuilding = edificio;
        selectedAula = null;
        if (confirmClassBtn) confirmClassBtn.disabled = true;
        updateSelectedLabel("Elige piso y aula.");

        buildingListEl
          .querySelectorAll(".pill-btn")
          .forEach((b) => b.classList.remove("active"));
        btn.classList.add("active");

        renderFloorsForBuilding(edificio);
      });

      buildingListEl.appendChild(btn);
    });

    const first = buildingListEl.querySelector(".pill-btn");
    if (first) first.click();
  }

  function renderFloorsForBuilding(edificio) {
    if (!floorListEl) return;

    floorListEl.innerHTML = "";

    const pisosMap = aulasPorEdificio[edificio] || {};
    const pisosOrdenados = Object.keys(pisosMap).sort(
      (a, b) => Number(a) - Number(b)
    );

    pisosOrdenados.forEach((pisoKey) => {
      const pisoNum = Number(pisoKey);
      const aulas = pisosMap[pisoKey];

      const section = document.createElement("div");
      section.className = "floor-section";

      const title = document.createElement("h4");
      title.className = "floor-title";
      title.textContent = `Piso ${pisoNum}`;
      section.appendChild(title);

      const group = document.createElement("div");
      group.className = "pill-group";

      aulas.forEach((aula) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "pill-btn aula-btn";
        btn.textContent = aula.aula;

        btn.addEventListener("click", () => {
          selectedAula = aula;
          if (confirmClassBtn) confirmClassBtn.disabled = false;
          updateSelectedLabel(
            `Has seleccionado: ${aula.edificio} · Piso ${aula.piso} · Aula ${aula.aula}`
          );

          floorListEl
            .querySelectorAll(".aula-btn")
            .forEach((b) => b.classList.remove("active"));
          btn.classList.add("active");
        });

        group.appendChild(btn);
      });

      section.appendChild(group);
      floorListEl.appendChild(section);
    });
  }

  function updateSelectedLabel(textIfEmpty) {
    if (!modalSelectedLabel) return;
    if (!selectedAula) {
      modalSelectedLabel.textContent = textIfEmpty || "";
      return;
    }
    modalSelectedLabel.textContent = `Has seleccionado: ${selectedAula.edificio} · Piso ${selectedAula.piso} · Aula ${selectedAula.aula}`;
  }

  if (confirmClassBtn) {
    confirmClassBtn.addEventListener("click", () => {
      if (!selectedAula) return;

      const label = `${selectedAula.edificio} - Piso ${selectedAula.piso} - ${selectedAula.aula}`;
      if (classroomInput) {
        classroomInput.value = label;
      }

      if (classModal) {
        classModal.hidden = true;
      }
    });
  }

  // ====== MENÚ DEL DÍA ======

  function resolveImageUrl(raw) {
    if (!raw) return "/img/default-snack.jpg";
    if (
      raw.startsWith("http://") ||
      raw.startsWith("https://") ||
      raw.startsWith("/")
    ) {
      return raw;
    }
    return `/img/${raw}`;
  }

  async function loadMenu() {
    if (!productListEl) return;

    try {
      const res = await fetch(`${API_BASE}/menu`);
      if (!res.ok) {
        throw new Error("No se pudo cargar el menú.");
      }

      const data = await res.json();
      const activos = (data || []).filter(
        (item) => (item.estado || "").toUpperCase() === "ACTIVO"
      );

      if (activos.length === 0) {
        productListEl.innerHTML =
          "<p>No hay productos activos en el menú del día.</p>";
        return;
      }

      renderMenu(activos);
    } catch (err) {
      console.error(err);
      productListEl.innerHTML = "<p>Ocurrió un error al cargar el menú.</p>";
    }
  }

  function renderMenu(items) {
    productListEl.innerHTML = "";

    items.forEach((item) => {
      const card = document.createElement("article");
      card.className = "product-card";

      const price = Number(item.precio || 0).toFixed(2);
      const stockText =
        item.stock != null ? `Stock: ${item.stock}` : "Stock disponible";
      const imgSrc = resolveImageUrl(item.imageUrl);

      card.innerHTML = `
        <div class="product-image-wrapper">
          <img src="${imgSrc}" alt="${item.nombreProducto || "Producto"}" class="product-image" />
        </div>

        <div class="product-body">
          <div class="product-name">${item.nombreProducto || "Producto"}</div>
          <div class="product-desc">
            ${item.descripcion || "Delicioso snack listo para tu aula."}
          </div>

          <div class="product-meta">
            <span class="product-price">Bs ${price}</span>
            <span class="product-stock">${stockText}</span>
          </div>

          <div class="product-actions">
            <div class="qty-control qty-control--product">
              <button type="button" class="qty-btn" data-action="dec">-</button>
              <span class="qty-value">0</span>
              <button type="button" class="qty-btn" data-action="inc">+</button>
            </div>
            <button type="button" class="btn btn-primary btn-add" disabled>
              Añadir
            </button>
          </div>
        </div>
      `;

      const qtyControl = card.querySelector(".qty-control--product");
      const qtyValueEl = qtyControl.querySelector(".qty-value");
      const decBtn = qtyControl.querySelector('[data-action="dec"]');
      const incBtn = qtyControl.querySelector('[data-action="inc"]');
      const addBtn = card.querySelector(".btn-add");

      let currentQty = 0;

      function updateQtyDisplay() {
        qtyValueEl.textContent = String(currentQty);
        addBtn.disabled = currentQty <= 0;
      }

      decBtn.addEventListener("click", () => {
        if (currentQty > 0) {
          currentQty -= 1;
          updateQtyDisplay();
        }
      });

      incBtn.addEventListener("click", () => {
        currentQty += 1;
        updateQtyDisplay();
      });

      addBtn.addEventListener("click", () => {
        if (currentQty <= 0) return;
        addToCart(item, imgSrc, currentQty);
        currentQty = 0;
        updateQtyDisplay();
      });

      updateQtyDisplay();
      productListEl.appendChild(card);
    });
  }

  // ====== CARRITO (modelo de datos) ======

  function updateCartBadge() {
    if (!cartBadgeEl) return;
    const totalQty = cart.reduce((sum, i) => sum + i.qty, 0);
    cartBadgeEl.textContent = totalQty;
    cartBadgeEl.style.display = totalQty > 0 ? "inline-flex" : "none";
  }

  function addToCart(product, imgSrc, amount = 1) {
    const qtyToAdd = Math.max(0, Number.isFinite(amount) ? amount : 0);
    if (qtyToAdd <= 0) return;

    const existing = cart.find((c) => c.id === product.id);
    if (existing) {
      existing.qty += qtyToAdd;
    } else {
      cart.push({
        id: product.id,
        nombre: product.nombreProducto || product.nombre || "Producto",
        precio: Number(product.precio || 0),
        qty: qtyToAdd,
        imageUrl: imgSrc || resolveImageUrl(product.imageUrl),
      });
    }
    renderCart();
  }

  function changeItemQty(id, delta) {
    const idx = cart.findIndex((c) => c.id === id);
    if (idx === -1) return;
    cart[idx].qty += delta;
    if (cart[idx].qty <= 0) {
      cart.splice(idx, 1);
    }
    renderCart();
  }

  function renderCart() {
    // ya no hay panel lateral, solo actualizamos badge + modal
    updateCartBadge();
    renderCartModal();
  }

  // ====== render modal carrito ======

  function renderCartModal() {
    if (!cartModalListEl || !cartModalTotalEl) return;

    cartModalListEl.innerHTML = "";

    if (cart.length === 0) {
      cartModalListEl.innerHTML =
        `<li class="cart-modal-item">Tu carrito está vacío.</li>`;
      cartModalTotalEl.textContent = "0.00";
      return;
    }

    cart.forEach((item) => {
      const li = document.createElement("li");
      li.className = "cart-modal-item";

      const totalItem = (item.precio * item.qty).toFixed(2);
      const imgSrc = item.imageUrl || "/img/default-snack.jpg";

      li.innerHTML = `
        <div class="cart-modal-left">
          <div class="cart-modal-thumb">
            <img src="${imgSrc}" alt="${item.nombre}">
          </div>
          <div>
            <div class="cart-modal-name">${item.nombre}</div>
            <div class="cart-modal-price">Bs ${item.precio.toFixed(2)} c/u</div>
          </div>
        </div>
        <div class="cart-modal-right">
          <div class="qty-control">
            <button type="button" class="qty-btn" data-action="dec">-</button>
            <span class="qty-value">${item.qty}</span>
            <button type="button" class="qty-btn" data-action="inc">+</button>
          </div>
          <div class="cart-modal-item-total">Bs ${totalItem}</div>
        </div>
      `;

      const decBtn = li.querySelector('[data-action="dec"]');
      const incBtn = li.querySelector('[data-action="inc"]');

      decBtn.addEventListener("click", () => {
        changeItemQty(item.id, -1);
      });

      incBtn.addEventListener("click", () => {
        changeItemQty(item.id, 1);
      });

      cartModalListEl.appendChild(li);
    });

    const total = cart.reduce((sum, i) => sum + i.precio * i.qty, 0);
    cartModalTotalEl.textContent = total.toFixed(2);
  }

  // abrir / cerrar modal carrito
  if (cartButton && cartModal) {
    cartButton.addEventListener("click", () => {
      setStatus("");
      renderCartModal();
      cartModal.hidden = false;
    });
  }

  if (closeCartModalBtn && cartModal) {
    closeCartModalBtn.addEventListener("click", () => {
      cartModal.hidden = true;
    });
  }

  // ====== CHECKOUT (creación REAL de pedido) ======

  async function performCheckout() {
    if (cart.length === 0) {
      setStatus("Tu carrito está vacío.", "error");
      return false;
    }

    // usuario logueado
    const user = getLoggedUser();
    if (!user || !user.id) {
      setStatus(
        "No se encontró el usuario en sesión. Vuelve a iniciar sesión.",
        "error"
      );
      return false;
    }

    // aula seleccionada
    if (!selectedAula || !selectedAula.id) {
      setStatus("Primero selecciona el aula.", "error");
      if (classModal) classModal.hidden = false;
      return false;
    }

    // opcional: mantener texto en el input visible
    if (classroomInput) {
      classroomInput.value =
        `${selectedAula.edificio} - Piso ${selectedAula.piso} - ${selectedAula.aula}`;
    }

    // construir payload para /orders
    const payload = {
      userId: user.id,
      aulaId: selectedAula.id,
      items: cart.map((c) => ({
        menuItemId: c.id,
        cantidad: c.qty,
      })),
    };

    setStatus("Enviando pedido...", "info");

    try {
      const res = await fetch(`${API_BASE}/orders`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errText = await res.text().catch(() => "");
        console.error("Error al crear pedido:", res.status, errText);
        throw new Error(errText || "No se pudo crear el pedido.");
      }

      const savedOrder = await res.json().catch(() => null);
      console.log("Pedido creado correctamente:", savedOrder);

      setStatus("Pedido enviado correctamente.", "success");
      cart = [];
      renderCart();
      return true;
    } catch (err) {
      console.error(err);
      setStatus("Ocurrió un error al enviar tu pedido.", "error");
      return false;
    }
  }

  if (cartModalCheckoutBtn && cartModal) {
    cartModalCheckoutBtn.addEventListener("click", async () => {
      const ok = await performCheckout();
      if (ok) {
        cartModal.hidden = true;
      }
    });
  }

  // === INICIALIZAR ===
  initClassroomModal();
  loadMenu();
});
