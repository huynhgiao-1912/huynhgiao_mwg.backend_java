package com.mwg.wb.rcm.flink.module.persionalize;

  
import lombok.extern.slf4j.Slf4j;
import mwg.rcm.model.tracking.DataTrackingBO;
import mwg.wb.analytics.helper.Json;
import lombok.val;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.Objects;

 
@Slf4j
public class JsonSerializer  implements SerializationSchema<DataTrackingBO> {

    @Override
    public byte[] serialize(final DataTrackingBO pojo) {
       byte[] result = new byte[0];
       if(Objects.nonNull(pojo)){
           try{
               val json = Json.toJson(pojo);
               result = json.getBytes();
           }
           catch(final Exception ex){
               log.error("", ex);
           }
       }
        return result;
    }
}
