package mwg.wb.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Upsertable {
	@JsonIgnore
	String indexValue();
}
