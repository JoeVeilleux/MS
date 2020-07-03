package com.joev.banking;

import java.io.Serializable;
import java.util.Optional;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Passenger implements Serializable {
    public static final long serialVersionUID = 1L;

    @JsonProperty
    public abstract String id();

    @JsonProperty
    public abstract String name();

    @JsonProperty
    public abstract String address();

    static Builder builder() {
        return new AutoValue_Passenger.Builder();
    }

    @AutoValue.Builder
    @JsonDeserialize(as = AutoValue_Passenger.Builder.class)
    public abstract static class Builder {

        @JsonProperty
        public abstract Builder id(String id);

        public abstract Optional<String> id();

        @JsonProperty
        public abstract Builder name(String name);

        @JsonProperty
        public abstract Builder address(String addr1);

        public abstract Passenger build();
    }
}
