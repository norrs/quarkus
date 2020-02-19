package io.quarkus.qute.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.reactivestreams.Publisher;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateData;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;

public class PublisherTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(TemplateDataTest.Foo.class)
                    .addAsResource(new StringAsset("{foo.val} is not {foo.val.setScale(2,roundingMode)}"),
                            "templates/foo.txt"));

    @Inject
    Template foo;

    @Test
    public void testTemplateData() {
        Publisher<String> publisher = foo.data("roundingMode", RoundingMode.HALF_UP)
                .data("foo", new TemplateDataTest.Foo(new BigDecimal("123.4563"))).publisher();
        assertTrue(publisher instanceof Multi);
        assertEquals("123.4563 is not 123.46", Multi.createFrom().publisher(publisher)
                .collectItems().in(StringBuffer::new, StringBuffer::append)
                .onItem().apply(StringBuffer::toString)
                .await().indefinitely());
    }

    @TemplateData
    @TemplateData(target = BigDecimal.class)
    public static class Foo {

        public final BigDecimal val;

        public Foo(BigDecimal val) {
            this.val = val;
        }

    }
}