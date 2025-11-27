"use strict";

document.addEventListener("DOMContentLoaded", () => {
  const registerForm = document.getElementById("registerForm");
  const registerMessage = document.getElementById("registerMessage");

  if (!registerForm || !registerMessage) {
    return;
  }

  function showRegisterError(msg) {
    registerMessage.hidden = false;
    registerMessage.textContent = msg;
    registerMessage.classList.remove("success-message");
  }

  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    registerMessage.hidden = true;
    registerMessage.textContent = "";
    registerMessage.classList.remove("success-message");

    const codigoEstudiante =
      registerForm.elements["codigoEstudiante"].value.trim();
    const nombre = registerForm.elements["nombre"].value.trim();
    const password = registerForm.elements["password"].value;
    const confirmPassword = registerForm.elements["confirmPassword"].value;

    if (!codigoEstudiante || !nombre || !password || !confirmPassword) {
      showRegisterError("Por favor completa todos los campos.");
      return;
    }

    if (password !== confirmPassword) {
      showRegisterError("Las contraseñas no coinciden.");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/users`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          id: Number(codigoEstudiante), // usamos código como id (BIGINT)
          nombre,
          password,
          rol: "USER",
        }),
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        const msg = data.message || "No se pudo crear la cuenta.";
        showRegisterError(msg);
        return;
      }

      registerMessage.hidden = false;
      registerMessage.textContent =
        "Cuenta creada correctamente. Ya puedes iniciar sesión.";
      registerMessage.classList.add("success-message");

      setTimeout(() => {
        registerForm.reset();
      }, 2000);
    } catch (err) {
      console.error(err);
      showRegisterError("Error de conexión con el servidor.");
    }
  });
});
