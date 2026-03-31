document.getElementById("btn-login").addEventListener("click", doLogin);

async function doLogin() {
	const inputData = {
		username: document.getElementById("username").value,
		password: document.getElementById("password").value
	};

	const payload = { input: inputData };
	const url = '/rest/login';

	try {
		const response = await fetch(url, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		});

		const reply = await response.json();
		const divResult = document.getElementById("OpResult");

		if (response.status === 200 && reply.status === "success") {
			sessionStorage.setItem("authToken", JSON.stringify(reply.data.token));
			divResult.textContent = 'Sessão iniciada com sucesso. Token armazenado.';
		}
		else
			divResult.textContent = 'Erro no login: [' + reply.status + '] ' + reply.data;
	} catch (error) {
		console.error('Erro no pedido:', error);
		document.getElementById("OpResult").textContent = 'Erro de comunicação com o servidor.';
	}
}