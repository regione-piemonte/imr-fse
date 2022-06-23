package it.csi.dmass.scaricoStudi.model;

public class Setting{

	private Long Id;
	private String asl;
	private String key;
	private String value;	
	
	public static final String IMR_RICH_ELAB_ERR = "IMR_RICH_ELAB_ERR";
	public static final String IMR_RICH_ELAB = "IMR_RICH_ELAB";
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getAsl() {
		return asl;
	}
	public void setAsl(String asl) {
		this.asl = asl;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "Setting [Id=" + Id + ", asl=" + asl + ", key=" + key + ", value=" + value + "]";
	}	
	
	
	
	
	
}
