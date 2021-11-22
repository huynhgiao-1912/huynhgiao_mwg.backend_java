package mwg.wb.analytics.helper;

  
import java.util.Objects;

import org.apache.flink.api.common.serialization.SerializationSchema;

import mwg.wb.analytics.DataTrackingBO;
import mwg.wb.analytics.helper.Json;
 

  
public class JsonSerializer  implements SerializationSchema<DataTrackingBO> {

    @Override
    public byte[] serialize(final DataTrackingBO pojo) {
       byte[] result = new byte[0];
       if(Objects.nonNull(pojo)){
           try{
               String json = Json.toJson(pojo);
               result = json.getBytes();
           }
           catch(final Exception ex){
              // log.error("", ex);
           }
       }
        return result;
    }
}
