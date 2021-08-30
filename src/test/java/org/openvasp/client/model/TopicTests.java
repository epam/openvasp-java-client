package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TopicTests {

    @Test
    public void topicFormatTest() {
        Assertions.assertEquals(10, Topic.newRandom().toString().length());
    }
}
