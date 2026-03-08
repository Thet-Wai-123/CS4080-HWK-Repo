//> Functions lox-function
package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  final Stmt.Function declaration;
  // > closure-field
  final Environment closure;

  // < closure-field
  /*
   * Functions lox-function < Functions closure-constructor
   * LoxFunction(Stmt.Function declaration) {
   */
  /*
   * Functions closure-constructor < Classes is-initializer-field
   * LoxFunction(Stmt.Function declaration, Environment closure) {
   */
  // > Classes is-initializer-field
  final boolean isInitializer;
  private LoxFunction innerMethod;

  LoxFunction(Stmt.Function declaration, Environment closure,
      boolean isInitializer) {
    this.isInitializer = isInitializer;
    // < Classes is-initializer-field
    // > closure-constructor
    this.closure = closure;
    // < closure-constructor
    this.declaration = declaration;
  }

  // > Classes bind-instance
  void setInnerMethod(LoxFunction inner) {
    this.innerMethod = inner;
  }

  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);

    // Bind 'inner' to point to the subclass method if it exists
    if (innerMethod != null) {
      LoxFunction boundInner = innerMethod.bind(instance);
      environment.define("inner", boundInner);
    }

    /*
     * Classes bind-instance < Classes lox-function-bind-with-initializer
     * return new LoxFunction(declaration, environment);
     */
    // > lox-function-bind-with-initializer
    LoxFunction boundMethod = new LoxFunction(declaration, environment,
        isInitializer);
    boundMethod.setInnerMethod(innerMethod);
    return boundMethod;
    // < lox-function-bind-with-initializer
  }

  // < Classes bind-instance
  // > function-to-string
  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }

  // < function-to-string
  // > function-arity
  @Override
  public int arity() {
    return declaration.params.size();
  }

  // < function-arity
  // > function-call
  @Override
  public Object call(Interpreter interpreter,
      List<Object> arguments) {
    /*
     * Functions function-call < Functions call-closure
     * Environment environment = new Environment(interpreter.globals);
     */
    // > call-closure
    Environment environment = new Environment(closure);
    // < call-closure
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme,
          arguments.get(i));
    }

    /*
     * Functions function-call < Functions catch-return
     * interpreter.executeBlock(declaration.body, environment);
     */
    // > catch-return
    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      // > Classes early-return-this
      if (isInitializer)
        return closure.getAt(0, "this");

      // < Classes early-return-this
      return returnValue.value;
    }
    // < catch-return
    // > Classes return-this

    if (isInitializer)
      return closure.getAt(0, "this");
    // < Classes return-this
    return null;
  }
  // < function-call
}
