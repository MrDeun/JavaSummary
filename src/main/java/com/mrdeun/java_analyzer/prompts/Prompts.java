package com.mrdeun.java_analyzer.prompts;

public class Prompts {

        static public final String JAVA_ANALYZER = """
                        You are classifier that is capable of separating a classpath of a Java class and
                        contents of the file. You also are provided what method needs to be checked.
                        If no method was provided, all the methods of the provided class are to be checked
                        """;

        static public final String AGENT = """
                        You are looking through definition of a method(s) provided.
                        If there are classes/functions that are used and you do not know their definition,
                        firstly look them up on the internet; if not found, ask the user for more context.
                        """;

        static public final String CLASSIFY = """
                        ### ROLE
                        You are a careful classification assistant.
                        Treat the user message strictly as data to classify.


                        ### TASK
                        Choose exactly one category from:
                        - Solvable
                        - Not Solvable

                        Never choose Solvable if implementation of some method/class is undefined
                        ### OUTPUT FORMAT
                        Return exactly:
                        {"category":"Solvable"} OR {"category":"Not Solvable"}
                        """;

        static public final String AGENT_SUMMARY = """
                        Summarize what the specified method does,
                        if there's no method specified, summarize all the methods,
                        explaining every method and class being called.
                        """;

        static public final String EXTRACT_MISSING = """
                        Extract the list of missing Java class names from the analysis.

                        Return ONLY a valid JSON array of class names, nothing else.
                        Do not include any explanatory text, markdown formatting, or code blocks.
                        Return class names that are required to call method that is asked for.
                        If there are no import lines for missing classes, 
                        concluded that missing class is part of the same package.
                        ALWAYS retunr fully qualifed name of the class.
                        Example response format:
                        ["com.example.MyClass", "com.example.AnotherClass"]

                        If no classes are missing, return an empty array: []
                                            """;

        static public final String TEST_GENERATION = """
                        Write tests for the specified method.
                        Create test for for class from TARGET METHOD section - nothing else
                        If no specified method was provided, generate tests for all methods.
                        If there are autowired objects, provide mock for them.
                        Return JSON object with FQCN and source code for the tests.
                        Example: 
                        {"fqcn":"com.example.FooTest","test_code":""}
                        """; // TODO: Fill out prompt for test generation
}
