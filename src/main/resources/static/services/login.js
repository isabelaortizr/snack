"use strict";

// URL base del backend
const API_BASE = "http://localhost:8181";

document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");
  const errorMessageEl = document.getElementById("errorMessage");
  const roleButtons = document.querySelectorAll(".role-btn");
  const showRegisterBtn = document.getElementById("showRegisterBtn");
  const backToLoginBtn = document.getElementById("backToLoginBtn");

  let currentRole = "user"; // "user" | "admin"

  // Toggle usuario / admin
  roleButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      roleButtons.forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      currentRole = btn.dataset.role || "user";
    });
  });

  // --- LOGIN ---
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      if (errorMessageEl) {
        errorMessageEl.hidden = true;
        errorMessageEl.textContent = "";
      }

      const formData = new FormData(loginForm);
      const idValue = formData.get("id");
      const passwordValue = formData.get("password");

      if (!idValue || !passwordValue) {
        if (errorMessageEl) {
          errorMessageEl.textContent = "Completa todos los campos.";
          errorMessageEl.hidden = false;
        }
        return;
      }

      try {
        const response = await fetch(`${API_BASE}/auth/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            id: Number(idValue),
            password: passwordValue,
          }),
        });

        if (!response.ok) {
          let msg = "Error al iniciar sesión. Intenta de nuevo.";
          if (response.status === 404) msg = "Usuario no encontrado.";
          else if (response.status === 401) msg = "Contraseña incorrecta.";

          if (errorMessageEl) {
            errorMessageEl.textContent = msg;
            errorMessageEl.hidden = false;
          }
          return;
        }

        const data = await response.json();
        console.log("Login OK:", data);

        // --- guardar sesión en localStorage para el dashboard ---
        try {
          // Para tu LoginResponse: { id, nombre, rol }
          const toStore = {
            id: data.id,
            nombre: data.nombre,
            rol: String(data.rol || "").toUpperCase(),
          };

          if (!toStore.id) {
            throw new Error("Respuesta sin id de usuario");
          }

          // limpiamos cualquier sesión previa y guardamos la nueva
          localStorage.removeItem("snackUser");
          localStorage.setItem("snackUser", JSON.stringify(toStore));
        } catch (e) {
          console.error("No se pudo guardar la sesión (snackUser)", e);
        }

        const rol = String(data.rol || "").toUpperCase();

        // Validar que el rol coincida con lo que eligió en la UI
        if (currentRole === "admin" && rol !== "ADMIN") {
          if (errorMessageEl) {
            errorMessageEl.textContent =
              "Esta cuenta no tiene permisos de administrador.";
            errorMessageEl.hidden = false;
          }
          return;
        }

        if (currentRole === "user" && rol !== "USER") {
          if (errorMessageEl) {
            errorMessageEl.textContent =
              "Esta cuenta es de administrador. Usa la pestaña Admin.";
            errorMessageEl.hidden = false;
          }
          return;
        }

        // Redirigir según el rol real del usuario
        if (rol === "ADMIN") {
          window.location.href = "/html/admin-dashboard.html";
        } else {
          window.location.href = "/html/user-dashboard.html";
        }
      } catch (err) {
        console.error(err);
        if (errorMessageEl) {
          errorMessageEl.textContent = "Error de red o servidor.";
          errorMessageEl.hidden = false;
        }
      }
    });
  }

  // --- TOGGLE LOGIN / REGISTRO ---
  if (showRegisterBtn && loginForm && registerForm) {
    showRegisterBtn.addEventListener("click", () => {
      loginForm.hidden = true;
      registerForm.hidden = false;
    });
  }

  if (backToLoginBtn && loginForm && registerForm) {
    backToLoginBtn.addEventListener("click", () => {
      registerForm.hidden = true;
      loginForm.hidden = false;
    });
  }
});
