package util;

import java.util.List;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class JsonMatcher extends BaseMatcher {

        Object expected;
        String diff;

        Configuration config = Configuration.empty()
            .withOptions(List.of(Option.IGNORING_ARRAY_ORDER));

        public JsonMatcher(Object expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actual) {
            Diff actual1 = Diff
                .create(expected, actual, "actual", "", config);
            boolean similar = actual1.similar();
            if (!similar) {
                diff = actual1.differences();
            }
            return similar;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(diff);
        }
}
