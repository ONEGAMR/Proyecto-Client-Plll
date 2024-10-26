package domain;

import java.time.LocalDate;

public class Student {
	private String carnet;
	private String nombre;
	private String correoElectronico;
	private int telefono;
	private boolean estaActivo;
	private LocalDate fechaIngreso;
	private char genero;
	private double dineroDisponible;

	// Constructor
	public Student(String carnet, String nombre, String correoElectronico, int telefono,
                   boolean estaActivo, LocalDate fechaIngreso, char genero, double dineroDisponible) {
		this.carnet = carnet;
		this.nombre = nombre;
		this.correoElectronico = correoElectronico;
		this.telefono = telefono;
		this.estaActivo = estaActivo;
		this.fechaIngreso = fechaIngreso;
		this.genero = genero;
		this.dineroDisponible = dineroDisponible;
	}

	public Student() {};

	public Student(String carnet) {
		this.carnet = carnet;
	}

	public String getCarnet() {
		return carnet;
	}

	public void setCarnet(String carnet) {
		this.carnet = carnet;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreoElectronico() {
		return correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public int getTelefono() {
		return telefono;
	}

	public void setTelefono(int telefono) {
		this.telefono = telefono;
	}

	public boolean isEstaActivo() {
		return estaActivo;
	}

	public void setEstaActivo(boolean estaActivo) {
		this.estaActivo = estaActivo;
	}

	public LocalDate getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(LocalDate fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}

	public char getGenero() {
		return genero;
	}

	public void setGenero(char genero) {
		this.genero = genero;
	}

	public double getDineroDisponible() {
		return dineroDisponible;
	}

	public void setDineroDisponible(double dineroDisponible) {
		this.dineroDisponible = dineroDisponible;
	}

	@Override
	public String toString() {
		return "Student [carnet=" + carnet + ", nombre=" + nombre + ", correoElectronico=" + correoElectronico
				+ ", telefono=" + telefono + ", estaActivo=" + estaActivo + ", fechaIngreso=" + fechaIngreso
				+ ", genero=" + genero + ", dineroDisponible=" + dineroDisponible + "]";
	}

	public String toStringUserData() {
		return
				carnet + "," + nombre + "," + correoElectronico + "," +
				telefono + "," + estaActivo + "," + fechaIngreso + "," + genero + "," + dineroDisponible;
	}
}