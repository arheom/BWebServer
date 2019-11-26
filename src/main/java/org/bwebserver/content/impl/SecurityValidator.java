package org.bwebserver.content.impl;

/**
 * Validates the user input, to be safe for the content service. Just basic validation
 * is done inside this basic code
 */
class SecurityValidator {
    static boolean isUserInputSafe(String userUrl){
        boolean isUrlSecure = !userUrl.equals("/") &&
                !userUrl.equals("") &&
                !userUrl.contains("/..") &&
                !userUrl.contains("../");
        return isUrlSecure;
    }
}
