package es.xpressaly.Service;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SanitizationService {
    private static final Logger logger = LoggerFactory.getLogger(SanitizationService.class);

    private final PolicyFactory policy = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.IMAGES);

    public String sanitize(String input) {
        if (input == null) {
            logger.debug("Null input received for sanitization");
            return null;
        }

        String sanitized = policy.sanitize(input);
        if (logger.isDebugEnabled() && !input.equals(sanitized)) {
            logger.debug("Sanitized input from: {} to: {}", input, sanitized);
        }

        return sanitized;
    }
}
