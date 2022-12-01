package com.hero.nettychat.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.hero.nettychat.common.CommonResultCode;
import com.hero.nettychat.common.exception.JsonException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * server
 *
 * @author kang
 */
public class JsonUtils {

    /**
     * 默认日期时间格式
     */
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    public static final String DATE = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String TIME = "HH:mm:ss";

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final ObjectMapper OBJECT_MAPPER2 = new ObjectMapper();

    private static final ObjectMapper SORT_OBJECT_MAPPER = new ObjectMapper();

    static {
        //该特性决定了当反序列化时遇到未知属性（没有映射到属性，没有任何setter或者任何可以处理它的handler），是否应该抛出一个
        //JsonMappingException异常。这个特性一般式所有其他处理方法对未知属性处理都无效后才被尝试，属性保留未处理状态。
        //默认情况下，该设置是被打开的enable。所以一般情况下考虑兼容性，需要手设置为desable
        //@since 1.2
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //该特性决定parser将是否允许解析使用Java/C++ 样式的注释（包括'/'+'*' 和'//' 变量）。
        //由于JSON标准说明书上面没有提到注释是否是合法的组成，所以这是一个非标准的特性；
        //尽管如此，这个特性还是被广泛地使用。
        //注意：该属性默认是false，因此必须显式允许，即通过JsonParser.Feature.ALLOW_COMMENTS 配置为true。
        OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_COMMENTS);
        //这个特性决定parser是否将允许使用非双引号属性名字， （这种形式在Javascript中被允许，但是JSON标准说明书中没有）。
        //注意：由于JSON标准上需要为属性名称使用双引号，所以这也是一个非标准特性，默认是false的。
        //同样，需要设置JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES为true，打开该特性。
        OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        //该特性决定parser是否允许单引号来包住属性名称和字符串值。
        //注意：默认下，该属性也是关闭的。需要设置JsonParser.Feature.ALLOW_SINGLE_QUOTES为true
        OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        //该特性决定parser是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）。
        // 如果该属性关闭，则如果遇到这些字符，则会抛出异常。
        //JSON标准说明书要求所有控制符必须使用引号，因此这是一个非标准的特性。
        //注意：默认时候，该属性关闭的。需要设置：JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS为true。
        OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        //null的属性不序列化,默认会序列化null
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //决定序列化哪些,java标准bean不需要设置
        //OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        //序列化Date日期时以timestamps输出，默认true，fastJson默认也是将Date解析为timestamp,LocalDateTime会带T，如果没有特殊要求使用时间戳即可
        //或使用注解@JsonFormat(pattern = "yyyy-MM-dd'T' HH:mm:ss:SSS'Z'",timezone = "GMT+8")时间格式注解 类型必须是Date,否则不生效
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);

        //默认时间戳只支持Date若想支持java8的LocalDateTime变时间戳，等：
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        javaTimeModule.addSerializer(LocalDate.class, new CustomLocalDateSerializer());
        javaTimeModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        javaTimeModule.addSerializer(LocalTime.class, new CustomLocalTimeSerializer());
        javaTimeModule.addDeserializer(LocalTime.class, new CustomLocalTimeDeserializer());
        //各种module：https://www.jianshu.com/p/8c2464e35efd
        OBJECT_MAPPER.registerModule(javaTimeModule)
                .registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module());

        //默认时间戳只支持Date,如果想格式华Date为字符串可以这样或者参考：tip5
        //OBJECT_MAPPER.setDateFormat(new SimpleDateFormat(DATE_TIME));

        //tip5：或者需要将时间按特定格式序列化参考以下配置,yyyy-MM-dd HH:mm:ss
        /*
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME)));
        javaTimeModule.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat(DATE_TIME)));

        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME)));
        javaTimeModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer(
                DateDeserializers.DateDeserializer.instance, new SimpleDateFormat(DATE_TIME), DATE_TIME)
        );
        OBJECT_MAPPER.registerModule(javaTimeModule)
                .registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module());
        */

        //或直接使用jackson模块处理时间等,格式不是标准格式yyyy这样的而是[2021,3,12,23,45,11,818000000]
        /*OBJECT_MAPPER.registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())//处理java8的Optional/Stream等类型及其扩展类型
                .registerModule(new JavaTimeModule());//处理java8时间api，LocalDateTime会被序列化为：[2021,3,12,23,45,11,818000000]
                */

        //"@class":"com.king.demo.domain.User",不推荐设置
        //OBJECT_MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
        //        ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);


        //排序，多用于api加密鉴权等
        SORT_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SORT_OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_COMMENTS);
        SORT_OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        SORT_OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        SORT_OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        SORT_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SORT_OBJECT_MAPPER.configure(
                //列化Map时对key进行排序操作，默认false
                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true
        );
        //按字母顺序排序属性,默认false
        SORT_OBJECT_MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        //Date和java8的时间都按ms时间戳，也可参考OBJECT_MAPPER示例的几种处理方式
        JavaTimeModule sortJavaTimeModule = new JavaTimeModule();
        sortJavaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME)));
        sortJavaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE)));
        sortJavaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME)));
        sortJavaTimeModule.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat(DATE_TIME)));

        sortJavaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME)));
        sortJavaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE)));
        sortJavaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME)));
        sortJavaTimeModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer(
                DateDeserializers.DateDeserializer.instance, new SimpleDateFormat(DATE_TIME), DATE_TIME)
        );
        SORT_OBJECT_MAPPER.registerModule(sortJavaTimeModule)
                .registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module());
    }

    private JsonUtils() {
    }

    public static String toJsonString(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonException(CommonResultCode.SERVER_ERROR, "JsonUtils.toJsonString.error", e);
        }
    }


    public static <T> T parseObject(String json, Class<T> valueType) {
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (Exception e) {
            throw new JsonException(CommonResultCode.SERVER_ERROR, "JsonUtils.parseObject.error", e);
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        try {
            return (T) OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new JsonException(CommonResultCode.SERVER_ERROR, "JsonUtils.parseObject.error", e);
        }
    }


    public static String toJsonStringSorted(Object obj) {
        try {
            return SORT_OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonException(CommonResultCode.SERVER_ERROR, "JsonUtils.toJsonStringSortByKeys.error", e);
        }
    }


    static class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator g, SerializerProvider provider) throws IOException {
            g.writeNumber(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        }
    }

    static class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            long timestamp = parser.getLongValue();
            return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        }
    }

    static class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate localDate, JsonGenerator g, SerializerProvider provider) throws IOException {
            g.writeNumber(localDate.atStartOfDay().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        }
    }

    static class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            long timestamp = parser.getLongValue();
            return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDate();
        }
    }

    static class CustomLocalTimeSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime localTime, JsonGenerator g, SerializerProvider provider) throws IOException {
            g.writeNumber(localTime.atDate(LocalDate.now()).toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        }
    }

    static class CustomLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            long timestamp = parser.getLongValue();
            return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalTime();
        }
    }

}


