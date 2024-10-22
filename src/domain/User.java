package domain;

import java.time.LocalDate;

public class User {

    private String carnet;
    private String nombre;
    private String correoElectronico;
    private int telefono;
    private boolean estaActivo;
    private LocalDate fechaIngreso;
    private char genero;
    private double dineroDisponible;
    private String password;

    public User() {}

    public User(String carnet) {
        this.carnet = carnet;
    }

    public User(String carnet, String nombre, String correoElectronico, int telefono, boolean estaActivo, LocalDate fechaIngreso, char genero, double dineroDisponible, String password) {
        this.carnet = carnet;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.estaActivo = estaActivo;
        this.fechaIngreso = fechaIngreso;
        this.genero = genero;
        this.dineroDisponible = dineroDisponible;
        this.password = password;
    }

    public User(String carnet, String nombre, String correoElectronico, int telefono, boolean estaActivo, LocalDate fechaIngreso, char genero, double dineroDisponible) {
        this.carnet = carnet;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.estaActivo = estaActivo;
        this.fechaIngreso = fechaIngreso;
        this.genero = genero;
        this.dineroDisponible = dineroDisponible;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "carnet='" + carnet + '\'' +
                ", nombre='" + nombre + '\'' +
                ", correoElectronico='" + correoElectronico + '\'' +
                ", telefono=" + telefono +
                ", estaActivo=" + estaActivo +
                ", fechaIngreso=" + fechaIngreso +
                ", genero=" + genero +
                ", dineroDisponible=" + dineroDisponible +
                ", password=" + password +
                '}';
    }
    public String toStringUserData() {
        return
                carnet + "," + nombre + "," + correoElectronico + "," +
                        telefono + "," + estaActivo + "," + fechaIngreso + "," + genero + "," + dineroDisponible+","+password;
    }
}
