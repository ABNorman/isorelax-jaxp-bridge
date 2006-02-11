package org.iso_relax.verifier.jaxp.validation;

import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.jaxp.validation.EntityResolverImpl;
import org.iso_relax.verifier.impl.ForkContentHandler;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.IOException;

/**
 * {@link Validator} implementation.
 *
 * This is a sloppy job, I know.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class ValidatorImpl extends Validator {
    private final Verifier verifier;
    private ErrorHandler errorHandler;
    private LSResourceResolver resourceResolver;
    private Transformer idTrans;


    ValidatorImpl(Verifier verifier) {
        this.verifier = verifier;
    }

    private Transformer getIdentitiyTransformer() {
        if(idTrans==null) {
            try {
                idTrans = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException e) {
                // impossible
                throw new InternalError();
            }
        }
        return idTrans;
    }

    public void validate(Source source, Result result) throws SAXException, IOException {
        if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource) source;
            if(result!=null)
                throw new IllegalArgumentException();

            InputSource is = new InputSource();
            is.setByteStream(ss.getInputStream());
            is.setCharacterStream(ss.getReader());
            is.setSystemId(ss.getSystemId());
            is.setPublicId(ss.getPublicId());
            verifier.verify(is);
        } else
        if (source instanceof DOMSource) {
            DOMSource ds = (DOMSource) source;
            verifier.verify(ds.getNode());
            if(result!=null) {
                try {
                    getIdentitiyTransformer().transform(source,result);
                } catch (TransformerException e) {
                    throw new SAXException(e);
                }
            }
        } else
        if (source instanceof SAXSource) {
            SAXSource ss = (SAXSource) source;
            XMLReader r = ss.getXMLReader();
            if(r==null) r = XMLReaderFactory.createXMLReader();

            ContentHandler h = verifier.getVerifierHandler();

            if (result instanceof SAXResult) {
                SAXResult sr = (SAXResult) result;
                h = new ForkContentHandler(h,sr.getHandler());
            }

            r.setContentHandler(h);

            if(ss.getInputSource()!=null)
                r.parse(ss.getInputSource());
            else
            if(ss.getSystemId()!=null)
                r.parse(ss.getSystemId());
            else
                throw new IllegalArgumentException();

        } else
            throw new IllegalArgumentException();
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        verifier.setErrorHandler(errorHandler);
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setResourceResolver(LSResourceResolver resourceResolver) {
        verifier.setEntityResolver(new EntityResolverImpl(resourceResolver));
        this.resourceResolver = resourceResolver;
    }

    public LSResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public void reset() {
        setErrorHandler(null);
        setResourceResolver(null);
    }
}
