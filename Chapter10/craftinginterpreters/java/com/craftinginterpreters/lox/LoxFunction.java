//> Functions lox-function
package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  // > closure-field
  private final Environment closure;

  // < closure-field
  // Function expression fields (for anonymous functions)
  private final List<Token> params;
  private final List<Stmt> body;
  private final boolean isExpression;

  /*
   * Functions lox-function < Functions closure-constructor
   * LoxFunction(Stmt.Function declaration) {
   */
  /*
   * Functions closure-constructor < Classes is-initializer-field
   * LoxFunction(Stmt.Function declaration, Environment closure) {
   */
  // > Classes is-initializer-field
  private final boolean isInitializer;

  LoxFunction(Stmt.Function declaration, Environment closure,
      boolean isInitializer) {
    this.isInitializer = isInitializer;
    // < Classes is-initializer-field
    // > closure-constructor
    this.closure = closure;
    // < closure-constructor
    this.declaration = declaration;
    this.params = null;
    this.body = null;
    this.isExpression = false;
  }

  // Constructor for anonymous function expressions
  LoxFunction(List<Token> params, List<Stmt> body, Environment closure) {
    this.params = params;
    this.body = body;
    this.closure = closure;
    this.declaration = null;
    this.isInitializer = false;
    this.isExpression = true;
  }

  // > Classes bind-instance
  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    /*
     * Classes bind-instance < Classes lox-function-bind-with-initializer
     * return new LoxFunction(declaration, environment);
     */
    // > lox-function-bind-with-initializer
    return new LoxFunction(declaration, environment,
        isInitializer);
    // < lox-function-bind-with-initializer
  }

  // < Classes bind-instance
  // > function-to-string
  @Override
  public String toString() {
    if (isExpression)
      return "<fn>";
    return "<fn " + declaration.name.lexeme + ">";
  }

  // < function-to-string
  // > function-arity
  @Override
  public int arity() {
    if (isExpression)
      return params.size();
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

    if (isExpression) {
      // Handle function expressions
      for (int i = 0; i < params.size(); i++) {
        environment.define(params.get(i).lexeme, arguments.get(i));
      }

      try {
        interpreter.executeBlock(body, environment);
      } catch (Return returnValue) {
        return returnValue.value;
      }
    } else {
      // Handle function declarations
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
    }
    return null;
  }
  // < function-call
}
