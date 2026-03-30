package pt.unl.fct.di.adc.firstwebapp.util;

public class RegisterData {

	public String username;
	public String password;
	public String confirmation;

	public String phone;
	public String address;
	public String role;

	public RegisterData() {

	}

	public RegisterData(String username, String password, String confirmation,
			String phone, String address, String role) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;

		this.phone = phone;
		this.address = address;
		this.role = role;
	}

	private boolean nonEmptyOrBlankField(String field) {
		return field != null && !field.isBlank();
	}

	public boolean validRegistration() {

		return nonEmptyOrBlankField(username) &&
				nonEmptyOrBlankField(password) &&
				username.contains("@") &&
				password.equals(confirmation) &&
				(nonEmptyOrBlankField(role) &&
						(role.equals("USER") ||
								role.equals("BOFFICER") ||
								role.equals("ADMIN")))
				&&
				nonEmptyOrBlankField(phone) && phone.matches("\\d{9}");
	}
}