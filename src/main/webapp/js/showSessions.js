document.getElementById("btn-show-sessions").addEventListener("click", showSessions);

async function showSessions() {
  const tokenStr = sessionStorage.getItem("authToken");
  const divResult = document.getElementById("OpResult");

  if (!tokenStr) return divResult.textContent = "Erro: Sem token de sessão.";

  const payload = { input: {}, token: JSON.parse(tokenStr) };

  try {
    const response = await fetch('/rest/showauthsessions', {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const reply = await response.json();
    divResult.textContent = reply.status === "success"
      ? JSON.stringify(reply.data.sessions, null, 2)
      : 'Erro: [' + reply.status + '] ' + reply.data;
  } catch (error) {
    divResult.textContent = 'Erro de comunicação.';
  }
}