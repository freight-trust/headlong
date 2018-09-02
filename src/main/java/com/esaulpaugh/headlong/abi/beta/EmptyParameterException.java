package com.esaulpaugh.headlong.abi.beta;

import java.text.ParseException;

class EmptyParameterException extends ParseException {

    /**
     * Constructs a ParseException with the specified detail message and
     * offset.
     * A detail message is a String that describes this particular exception.
     *
     * @param s           the detail message
     * @param errorOffset the position where the error is found while parsing.
     */
    EmptyParameterException(String s, int errorOffset) {
        super(s, errorOffset);
    }
}
