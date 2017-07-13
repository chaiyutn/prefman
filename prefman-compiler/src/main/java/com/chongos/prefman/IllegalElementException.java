package com.chongos.prefman;

import javax.lang.model.element.Element;

/**
 * @author ChongOS
 * @since 07-Jul-2017
 */
class IllegalElementException extends RuntimeException {

  private Element element;

  IllegalElementException(String s, Element element) {
    super(s);
    this.element = element;
  }

  Element getElement() {
    return element;
  }
}
