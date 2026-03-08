//> Classes lox-instance
package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
  private LoxClass klass;
  // > lox-instance-fields
  private final Map<String, Object> fields = new HashMap<>();
  // < lox-instance-fields

  LoxInstance(LoxClass klass) {
    this.klass = klass;
  }

  // > lox-instance-get-property
  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    // Check if it's a getter first
    LoxFunction getter = klass.findGetter(name.lexeme);
    if (getter != null) {
      // Call the getter immediately and return its result
      // For simplicity, we create a temporary interpreter here
      // In a full implementation, we'd pass the interpreter through the call chain
      return getter.bind(this).call(new Interpreter(), new java.util.ArrayList<>());
    }

    // > lox-instance-get-method
    LoxFunction method = klass.findMethod(name.lexeme);
    /*
     * Classes lox-instance-get-method < Classes lox-instance-bind-method
     * if (method != null) return method;
     */
    // > lox-instance-bind-method
    if (method != null)
      return method.bind(this);
    // < lox-instance-bind-method

    // < lox-instance-get-method
    throw new RuntimeError(name, // [hidden]
        "Undefined property '" + name.lexeme + "'.");
  }

  // < lox-instance-get-property
  // > lox-instance-set-property
  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  // < lox-instance-set-property
  @Override
  public String toString() {
    return klass.name + " instance";
  }
}
