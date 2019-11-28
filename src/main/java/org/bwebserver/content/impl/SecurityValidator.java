package org.bwebserver.content.impl;

/**
 * Validates the user input, to be safe for the content service. Just basic validation
 * is done inside this basic code
 */
class SecurityValidator {
    static boolean isUserInputSafe(String userUrl){
        boolean isUrlSecure = !userUrl.contains("/../");
        return isUrlSecure;
    }
}
