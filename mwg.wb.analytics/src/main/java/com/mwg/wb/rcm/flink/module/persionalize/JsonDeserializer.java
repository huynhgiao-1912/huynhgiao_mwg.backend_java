package com.mwg.wb.rcm.flink.module.persionalize;

import lombok.extern.slf4j.Slf4j;
import mwg.rcm.model.tracking.DataTrackingBO;
import mwg.wb.analytics.helper.Json;
import lombok.val;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;

import java.io.IOException;
import java.util.Objects;

 
@Slf4j
public class  JsonDeserializer implements DeserializationSchema<DataTrackingBO> {

    @Override
    public DataTrackingBO deserialize(final byte[] bytes) throws IOException {
        if(Objects.isNull(bytes)){
            return null;
        }
        DataTrackingBO click = null;
        try{
            val json = new String(bytes);
            click = Json.toObject(json, DataTrackingBO.class);
        }
        catch(final Exception ex){
            log.error("", ex);
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
