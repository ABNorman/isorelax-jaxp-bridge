package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.VerifierConfigurationException;

import javax.xml.XMLConstants;

/**
 * @author Kohsuke Kawaguchi
 */
public class RELAXNGSchemaFactoryImpl extends SchemaFactoryImpl {
    public RELAXNGSchemaFactoryImpl() throws VerifierConfigurationException {
        super();
    }

    protected String getLanguageName() {
        return XMLConstants.RELAXNG_NS_URI;
    }
}
