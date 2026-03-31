document.getElementById("btn-register").addEventListener("click", registerAccount);

async function registerAccount() {
	const inputData = {
		username: document.getElementById("username").value,
		password: document.getElementById("password").value,
		confirmation: document.getElementById("confirmation").value,
		phone: document.getElementById("phone").value,
		address: document.getElementById("address").value,
		role: document.getElementById("role").value
	};

	const payload = { input: inputData };
	const url = '/rest/createaccount';

	try {
		const response = await fetch(url, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		});

		const reply = await response.json();
		const divResult = document.getElementById("OpResult");

		if (response.status === 200 && reply.status === "success") {
			divResult.textContent = 'Conta criada com sucesso para: ' + reply.data.username;
			clearForm();
		}
		else
			divResult.textContent = 'Erro ao criar conta: [' + reply.status + '] ' + reply.data;
	} catch (error) {
		console.error('Erro no pedido:', error);
		document.getElementById("OpResult").textContent = 'Erro de comunicação com o servidor.';
	}
}

function clearForm() {
	document.getElementById("username").value = "";
	document.getElementById("password").value = "";
	document.getElementById("confirmation").value = "";
	document.getElementById("phone").value = "";
	document.getElementById("address").value = "";
}