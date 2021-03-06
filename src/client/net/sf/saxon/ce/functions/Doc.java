package client.net.sf.saxon.ce.functions;
import client.net.sf.saxon.ce.expr.*;
import client.net.sf.saxon.ce.om.Item;
import client.net.sf.saxon.ce.om.NodeInfo;
import client.net.sf.saxon.ce.trans.XPathException;
import client.net.sf.saxon.ce.value.AtomicValue;

/**
 * Implement the fn:doc() function - a simplified form of the Document function
 */

public class Doc extends SystemFunction {

    public Doc newInstance() {
        return new Doc();
    }

    private String expressionBaseURI = null;
    private boolean readOnce = false;

    /**
     * Indicate that the document will be read once only (or that it should be treated as if it
     * is read once only. This means (a) the document will not be held in memory after all references
     * to it go out of scope, and (b) if the query or transformation tries to read it again, it will get a new
     * copy, with different node identities, and potentially with different content. It also means that the
     * document is eligible for document projection.
     * @param once true if this document is to be treated as being read once only
     */

    public void setReadOnce(boolean once) {
        readOnce = once;
    }

    /**
     * Ask whether this document has been marked as being read once only.
     * @return true if the document has been marked as being read once only
     */

    public boolean isReadOnce() {
        return readOnce;
    }

    public void checkArguments(ExpressionVisitor visitor) throws XPathException {
        if (expressionBaseURI == null) {
            super.checkArguments(visitor);
            expressionBaseURI = visitor.getStaticContext().getBaseURI();
        }
    }

    /**
     * Get the static base URI of the expression
     * @return the static base URI
     */

    public String getStaticBaseURI() {
        return expressionBaseURI;
    }

    /**
     * preEvaluate: this method suppresses compile-time evaluation unless a configuration option has been
     * set to allow early evaluation.
     * @param visitor an expression visitor
     */

    public Expression preEvaluate(ExpressionVisitor visitor) {
         return this;
    }

    public int computeCardinality() {
        return argument[0].getCardinality() & ~StaticProperty.ALLOWS_MANY;
    }


    /**
     * Evaluate the expression
     * @param context the dynamic evaluation context
     * @return the result of evaluating the expression (a document node)
     * @throws XPathException
     */

    public Item evaluateItem(XPathContext context) throws XPathException {
        return doc(context);
    }

    /**
    * Get the static properties of this expression (other than its type). The result is
    * bit-signficant. These properties are used for optimizations. In general, if
    * property bit is set, it is true, but if it is unset, the value is unknown.
     */

    public int computeSpecialProperties() {
        return StaticProperty.ORDERED_NODESET |
                StaticProperty.PEER_NODESET |
                StaticProperty.NON_CREATIVE |
                StaticProperty.SINGLE_DOCUMENT_NODESET;
        // Declaring it as a peer node-set expression avoids sorting of expressions such as
        // doc(XXX)/a/b/c
        // The doc() function might appear to be creative: but it isn't, because multiple calls
        // with the same arguments will produce identical results.
    }

    private NodeInfo doc(XPathContext context) throws XPathException {
        AtomicValue hrefVal = (AtomicValue)argument[0].evaluateItem(context);
        if (hrefVal==null) {
            return null;
        }
        String href = hrefVal.getStringValue();
        NodeInfo item = DocumentFn.makeDoc(href, expressionBaseURI, context, this.getSourceLocator());
        if (item==null) {
            // we failed to read the document
            dynamicError("Failed to load document " + href, "FODC0002", context);
            return null;
        }
        return item;
    }


}

// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is “Incompatible With Secondary Licenses”, as defined by the Mozilla Public License, v. 2.0.
