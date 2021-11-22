package mwg.wb.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.sim.SimBO;

@JsonInclude(Include.NON_DEFAULT)
public class SimBOApi extends SimBO {

}
