package mwg.wb.analytics.helper;

 

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;

import mwg.wb.analytics.DataTrackingBO;
import mwg.wb.analytics.helper.Json;

import java.io.IOException;
import java.util.Objects;

  
public class  JsonDeserializer implements DeserializationSchema<DataTrackingBO> {

    @Override
    public DataTrackingBO deserialize(final byte[] bytes) throws IOException {
        if(Objects.isNull(bytes)){
            return null;
        }
        DataTrackingBO click = null;
        try{
        	String json = new String(bytes);
            click = Json.toObject(json, DataTrackingBO.class);
        }
        catch(final Exception ex){
            //log.error("", ex);
        }
        return click;
    }

    @Override
    public boolean isEndOfStream(final DataTrackingBO nextElement) {
        return false;
    }

    //NOTE THIS.
    @Override
    public TypeInformation<DataTrackingBO> getProducedType() {
        return PojoTypeInfo.of(DataTrackingBO.class);
    }
}
