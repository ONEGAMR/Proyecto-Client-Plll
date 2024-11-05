package domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Recharge {

	private LocalDate date;
	private double monto;

	public Recharge() {}

	public Recharge(double monto, LocalDate date) {
		this.monto = monto;
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getDateTb() {

		return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public double getMonto() {
		return monto;
	}

	public void setMonto(double monto) {
		this.monto = monto;
	}
}
