document.getElementById("btn-show-users").addEventListener("click", showUsers);

async function showUsers() {
	const tokenStr = sessionStorage.getItem("authToken");
	const divResult = document.getElementById("OpResult");

	if (!tokenStr) {
		divResult.textContent = "Erro: Sessão não iniciada (Token inexistente).";
		return;
	}

	const payload = { input: {}, token: JSON.parse(tokenStr) };

	try {
		const response = await fetch('/rest/showusers', {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		});

		const reply = await response.json();
		if (response.status === 200 && reply.status === "success") {
			divResult.textContent = JSON.stringify(reply.data.users, null, 2);
		} else {
			divResult.textContent = 'Erro: [' + reply.status + '] ' + reply.data;
		}
	} catch (error) {
		divResult.textContent = 'Erro de comunicação.';
	}
}