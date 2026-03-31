document.getElementById("btn-role").addEventListener("click", showRole);

async function showRole() {
  const tokenStr = sessionStorage.getItem("authToken");
  const divResult = document.getElementById("OpResult");

  if (!tokenStr) return divResult.textContent = "Erro: Sem token de sessão.";

  const payload = {
    input: { username: document.getElementById("username").value },
    token: JSON.parse(tokenStr)
  };

  try {
    const response = await fetch('/rest/showuserrole', {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const reply = await response.json();
    divResult.textContent = reply.status === "success"
      ? 'Utilizador: ' + reply.data.username + ' | Role: ' + reply.data.role
      : 'Erro: [' + reply.status + '] ' + reply.data;
  } catch (error) {
    divResult.textContent = 'Erro de comunicação.';
  }
}