document.getElementById("btn-logout").addEventListener("click", logout);

async function logout() {
  const tokenStr = sessionStorage.getItem("authToken");
  const divResult = document.getElementById("OpResult");

  if (!tokenStr) return divResult.textContent = "Erro: Sem token de sessão.";

  const payload = {
    input: { username: document.getElementById("username").value },
    token: JSON.parse(tokenStr)
  };

  try {
    const response = await fetch('/rest/logout', {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const reply = await response.json();
    if (response.status === 200 && reply.status === "success") {
      sessionStorage.removeItem("authToken");
      divResult.textContent = 'Sucesso: ' + reply.data.message;
    } else {
      divResult.textContent = 'Erro: [' + reply.status + '] ' + reply.data;
    }
  } catch (error) {
    divResult.textContent = 'Erro de comunicação.';
  }
}