package xyz.oapps.osync.fields;

import java.util.ArrayList;
import java.util.List;

public class Fields {

	public static Fields of() {
		return new Fields();
	}

	List<TextField> textFields = new ArrayList<TextField>();
	
	List<NumberField> numberFields = new ArrayList<NumberField>();
	
	List<BooleanField> booleanFields = new ArrayList<BooleanField>();
	
	List<DoubleField> doubleFields = new ArrayList<DoubleField>();
	
	List<DateField> dateFields = new ArrayList<DateField>();
	
	List<DateTimeField> dateTimeFields = new ArrayList<DateTimeField>();

	public class TextField extends Field {
		public TextField(String id, String displayName, boolean mandatory) {
			super("text", id, displayName, mandatory);
		}
	}

	public class NumberField extends Field {
		public NumberField(String id, String displayName, boolean mandatory) {
			super("number", id, displayName, mandatory);
		}

	}

	public class BooleanField extends Field {
		public BooleanField(String id, String displayName, boolean mandatory) {
			super("boolean", id, displayName, mandatory);
		}
	}
	
	public class DoubleField extends Field {
		public DoubleField(String id, String displayName, boolean mandatory) {
			super("double", id, displayName, mandatory);
		}
	}
	
	public class DateField extends Field {
		public DateField(String id, String displayName, boolean mandatory, String format) {
			super("date", id, displayName, mandatory);
			this.setFormat(format);
		}
	}
	
	public class DateTimeField extends Field {
		public DateTimeField(String id, String displayName, boolean mandatory, String format) {
			super("datetime", id, displayName, mandatory);
			this.setFormat(format);
		}
	}

	public void addTextField(TextField tf) {
		getTextFields().add(tf);
	}
	
	public void addDoubleField(DoubleField df) {
		getDoubleFields().add(df);
	}

	public void addNumberField(NumberField nf) {
		getNumberFields().add(nf);
	}

	public void addBooleanField(BooleanField bf) {
		getBooleanFields().add(bf);
	}
	
	public void addDateField(DateField bf) {
		getDateFields().add(bf);
	}
	
	public void addDateTimeField(DateTimeField bf) {
		getDateTimeFields().add(bf);
	}
	
	public Fields number(String id, String displayName, boolean mandatory) {
		NumberField nf = new NumberField(id, displayName, mandatory);
		this.addNumberField(nf);
		return this;
	}

	public Fields text(String id, String displayName, boolean mandatory) {
		TextField nf = new TextField(id, displayName, mandatory);
		this.addTextField(nf);
		return this;
	}

	public Fields bool(String id, String displayName, boolean mandatory) {
		BooleanField nf = new BooleanField(id, displayName, mandatory);
		this.addBooleanField(nf);
		return this;
	}
	
	public Fields date(String id, String displayName, boolean mandatory, String format) {
		DateField nf = new DateField(id, displayName, mandatory, format);
		this.addDateField(nf);
		return this;
	}
	
	public Fields datetime(String id, String displayName, boolean mandatory, String format) {
		DateTimeField nf = new DateTimeField(id, displayName, mandatory, format);
		this.addDateTimeField(nf);
		return this;
	}

	public List<TextField> getTextFields() {
		return textFields;
	}
	
	public List<DoubleField> getDoubleFields() {
		return doubleFields;
	}

	public List<NumberField> getNumberFields() {
		return numberFields;
	}

	public List<BooleanField> getBooleanFields() {
		return booleanFields;
	}
	
	public List<DateField> getDateFields() {
		return dateFields;
	}
	
	public List<DateTimeField> getDateTimeFields() {
		return dateTimeFields;
	}

	public Fields doubleField(String id, String displayName, boolean mandatory) {
		DoubleField nf = new DoubleField(id, displayName, mandatory);
		this.addDoubleField(nf);
		return this;
	}
}
