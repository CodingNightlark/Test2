package mystudy;

    import com.google.gson.*;
    import com.google.gson.stream.JsonReader;
    import com.google.gson.stream.JsonWriter;

    import java.io.IOException;
    import java.time.LocalDate;
    import java.time.format.DateTimeFormatter;
    
    public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
        @Override
        public void write(JsonWriter out, LocalDate date) throws IOException {
            out.value(date.format(formatter));
        }
    
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString(), formatter);
        }

    
}
