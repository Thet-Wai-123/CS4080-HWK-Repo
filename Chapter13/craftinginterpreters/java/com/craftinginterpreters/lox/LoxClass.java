//> Classes lox-class
package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

/* Classes lox-class < Classes lox-class-callable
class LoxClass {
*/
//> lox-class-callable
class LoxClass implements LoxCallable {
  // < lox-class-callable
  final String name;
  // > Inheritance lox-class-superclass-field
  final LoxClass superclass;
  // < Inheritance lox-class-superclass-field
  /*
   * Classes lox-class < Classes lox-class-methods
   * 
   * LoxClass(String name) {
   * this.name = name;
   * }
   */
  // > lox-class-methods
  private final Map<String, LoxFunction> methods;

  /*
   * Classes lox-class-methods < Inheritance lox-class-constructor
   * LoxClass(String name, Map<String, LoxFunction> methods) {
   */
  // > Inheritance lox-class-constructor
  LoxClass(String name, LoxClass superclass,
      Map<String, LoxFunction> methods) {
    this.superclass = superclass;
    // < Inheritance lox-class-constructor
    this.name = name;
    this.methods = methods;
  }

  // < lox-class-methods
  // > lox-class-find-method
  LoxFunction findMethod(String name) {
    System.out.println("Finding method '" + name + "' in class " + this.name);
    // For BETA-style inheritance, start from the topmost superclass
    // and work down, collecting the method chain
    LoxFunction topMethod = findTopMethod(name);
    LoxFunction innerMethod = findInnerMethod(name);

    if (topMethod != null && innerMethod != null && topMethod != innerMethod) {
      // Create a copy with inner method set
      LoxFunction methodWithInner = new LoxFunction(topMethod.declaration,
          topMethod.closure, topMethod.isInitializer);
      methodWithInner.setInnerMethod(innerMethod);
      return methodWithInner;
    }

    return topMethod; // Return the top method even if there's no inner
  }

  private LoxFunction findTopMethod(String name) {
    // Start from the topmost superclass
    if (superclass != null) {
      LoxFunction superMethod = superclass.findTopMethod(name);
      if (superMethod != null) {
        return superMethod; // Return the topmost method
      }
    }

    // If no superclass has it, return our method
    return methods.get(name);
  }

  private LoxFunction findInnerMethod(String name) {
    // Find the first subclass method (immediate child)
    if (methods.containsKey(name)) {
      return methods.get(name);
    }
    return null; // No local method, so no inner
  }
  // < lox-class-find-method

  @Override
  public String toString() {
    return name;
  }

  // > lox-class-call-arity
  @Override
  public Object call(Interpreter interpreter,
      List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
    // > lox-class-call-initializer
    LoxFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    // < lox-class-call-initializer
    return instance;
  }

  @Override
  public int arity() {
    /*
     * Classes lox-class-call-arity < Classes lox-initializer-arity
     * return 0;
     */
    // > lox-initializer-arity
    LoxFunction initializer = findMethod("init");
    if (initializer == null)
      return 0;
    return initializer.arity();
    // < lox-initializer-arity
  }
  // < lox-class-call-arity
}
