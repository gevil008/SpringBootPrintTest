//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.springbootprinttest.utils.pdf;

import fr.opensagres.poi.xwpf.converter.core.*;
import fr.opensagres.poi.xwpf.converter.core.styles.XWPFStylesDocument;
import fr.opensagres.poi.xwpf.converter.core.utils.DxaUtil;
import fr.opensagres.poi.xwpf.converter.core.utils.StringUtils;
import fr.opensagres.poi.xwpf.converter.core.utils.XWPFRunHelper;
import fr.opensagres.poi.xwpf.converter.core.utils.XWPFTableUtil;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlTokenSource;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.HdrDocument.Factory;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.Enum;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class XWPFDocumentVisitor<T, O extends Options, E extends IXWPFMasterPage> implements IMasterPageHandler<E> {
    private static final Logger LOGGER = Logger.getLogger(XWPFDocumentVisitor.class.getName());
    protected static final String WORD_MEDIA = "word/media/";
    protected final XWPFDocument document;
    private final MasterPageManager masterPageManager;
    private XWPFHeader currentHeader;
    private XWPFFooter currentFooter;
    protected final XWPFStylesDocument stylesDocument;
    protected final O options;
    private boolean pageBreakOnNextParagraph;
    protected boolean processingTotalPageCountField = false;
    protected boolean totalPageFieldUsed = false;
    private Map<Integer, ListContext> listContextMap;

    public XWPFDocumentVisitor(XWPFDocument document, O options) throws Exception {
        this.document = document;
        this.options = options;
        this.stylesDocument = this.createStylesDocument(document);
        this.masterPageManager = new MasterPageManager(document.getDocument(), this);
    }

    protected XWPFStylesDocument createStylesDocument(XWPFDocument document) throws XmlException, IOException {
        return new XWPFStylesDocument(document);
    }

    @Override
    public XWPFStylesDocument getStylesDocument() {
        return this.stylesDocument;
    }

    public O getOptions() {
        return this.options;
    }

    public MasterPageManager getMasterPageManager() {
        return this.masterPageManager;
    }

    public void start() throws Exception {
        T container = this.startVisitDocument();
        List<IBodyElement> bodyElements = this.document.getBodyElements();
        this.visitBodyElements(bodyElements, container);
        this.endVisitDocument();
    }

    protected abstract T startVisitDocument() throws Exception;

    protected abstract void endVisitDocument() throws Exception;

    protected void visitBodyElements(List<IBodyElement> bodyElements, T container) throws Exception {
        if (!this.masterPageManager.isInitialized()) {
            this.masterPageManager.initialize();
        }

        for (int i = 0; i < bodyElements.size(); ++i) {
            IBodyElement bodyElement = bodyElements.get(i);
            switch (bodyElement.getElementType()) {
                case PARAGRAPH:
                    XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                    this.visitParagraph(paragraph, i, container);
                    break;
                case TABLE:
                    this.visitTable((XWPFTable) bodyElement, i, container);
                    break;
                case CONTENTCONTROL:
                    this.visitSDT((XWPFSDT) bodyElement, i, container);
            }
        }

    }

    protected void visitSDT(XWPFSDT contents, int index, T container) throws Exception {
        T sdtContainer = this.startVisitSDT(contents, container);
        this.visitSDTBody(contents, sdtContainer);
        this.endVisitSDT(contents, container, sdtContainer);
    }

    protected abstract T startVisitSDT(XWPFSDT var1, T var2) throws SAXException;

    protected abstract void endVisitSDT(XWPFSDT var1, T var2, T var3) throws SAXException;

    protected void visitSDTBody(XWPFSDT contents, T sdtContainer) throws Exception {
        ISDTContent content = contents.getContent();

        try {
            Field bodyElements = content.getClass().getDeclaredField("bodyElements");
            bodyElements.setAccessible(true);
            List<ISDTContents> isdtContents = (List) bodyElements.get(content);

            for (int i = 0; i < isdtContents.size(); ++i) {
                ISDTContents isdtContent = (ISDTContents) isdtContents.get(i);
                if (isdtContent instanceof XWPFParagraph) {
                    this.visitParagraph((XWPFParagraph) isdtContent, i, sdtContainer);
                } else if (isdtContent instanceof XWPFTable) {
                    this.visitTable((XWPFTable) isdtContent, i, sdtContainer);
                } else if (isdtContent instanceof XWPFRun) {
                    this.visitRun((XWPFParagraph) ((XWPFRun) isdtContent).getParent(), (XmlObject) isdtContent, sdtContainer);
                } else if (isdtContent instanceof XWPFSDT) {
                    this.visitSDT((XWPFSDT) isdtContent, i, sdtContainer);
                }
            }
        } catch (NoSuchFieldException var8) {
            var8.printStackTrace();
        } catch (IllegalAccessException var9) {
            var9.printStackTrace();
        }

    }

    protected void visitParagraph(XWPFParagraph paragraph, int index, T container) throws Exception {
        if (this.isWordDocumentPartParsing()) {
            this.masterPageManager.update(paragraph.getCTP());
        }

        if (this.pageBreakOnNextParagraph) {
            this.pageBreak();
        }

        this.pageBreakOnNextParagraph = false;
        ListItemContext itemContext = null;
        CTNumPr originalNumPr = this.stylesDocument.getParagraphNumPr(paragraph);
        CTNumPr numPr = this.getNumPr(originalNumPr);
        if (numPr != null) {
            XWPFNum num = this.getXWPFNum(numPr);
            if (num != null) {
                XWPFAbstractNum abstractNum = this.getXWPFAbstractNum(num);
                CTDecimalNumber ilvl = numPr.getIlvl();
                int level = ilvl != null ? ilvl.getVal().intValue() : 0;
                CTLvl lvl = abstractNum.getAbstractNum().getLvlArray(level);
                if (lvl != null) {
                    ListContext listContext = this.getListContext(originalNumPr.getNumId().getVal().intValue());
                    itemContext = listContext.addItem(lvl);
                }
            }
        }

        T paragraphContainer = this.startVisitParagraph(paragraph, itemContext, container);
        this.visitParagraphBody(paragraph, index, paragraphContainer);
        this.endVisitParagraph(paragraph, container, paragraphContainer);
    }

    private CTNumPr getNumPr(CTNumPr numPr) {
        if (numPr != null) {
            XWPFNum num = this.getXWPFNum(numPr);
            if (num != null) {
                XWPFAbstractNum abstractNum = this.getXWPFAbstractNum(num);
                CTString numStyleLink = abstractNum.getAbstractNum().getNumStyleLink();
                String styleId = numStyleLink != null ? numStyleLink.getVal() : null;
                if (styleId != null) {
                    CTStyle style = this.stylesDocument.getStyle(styleId);
                    CTPPr ppr = style.getPPr();
                    if (ppr == null) {
                        return null;
                    }

                    return this.getNumPr(ppr.getNumPr());
                }
            }
        }

        return numPr;
    }

    private ListContext getListContext(int numId) {
        if (this.listContextMap == null) {
            this.listContextMap = new HashMap();
        }

        ListContext listContext = (ListContext) this.listContextMap.get(numId);
        if (listContext == null) {
            listContext = new ListContext();
            this.listContextMap.put(numId, listContext);
        }

        return listContext;
    }

    protected abstract T startVisitParagraph(XWPFParagraph var1, ListItemContext var2, T var3) throws Exception;

    protected abstract void endVisitParagraph(XWPFParagraph var1, T var2, T var3) throws Exception;

    protected void visitParagraphBody(XWPFParagraph paragraph, int index, T paragraphContainer) throws Exception {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) {
            if (this.isAddNewLine(paragraph, index)) {
                this.visitEmptyRun(paragraphContainer);
            }
        } else {
            this.visitRuns(paragraph, paragraphContainer);
        }

        CTPPr ppr = paragraph.getCTP().getPPr();
        if (ppr != null && ppr.isSetPageBreakBefore()) {
            CTOnOff pageBreak = ppr.getPageBreakBefore();
            if (pageBreak != null && (pageBreak.getVal() == null || pageBreak.getVal().intValue() == 1)) {
                this.pageBreak();
            }
        }

    }

    protected XWPFNum getXWPFNum(CTNumPr numPr) {
        CTDecimalNumber numID = numPr.getNumId();
        if (numID == null) {
            return null;
        } else {
            if (this.document.getNumbering() != null) {
                XWPFNum num = this.document.getNumbering().getNum(numID.getVal());
                return num;
            }
            return null;
        }
    }

    protected XWPFAbstractNum getXWPFAbstractNum(XWPFNum num) {
        CTDecimalNumber abstractNumID = num.getCTNum().getAbstractNumId();
        XWPFAbstractNum abstractNum = this.document.getNumbering().getAbstractNum(abstractNumID.getVal());
        return abstractNum;
    }

    private boolean isAddNewLine(XWPFParagraph paragraph, int index) {
        IBody body = paragraph.getBody();
        List<IBodyElement> bodyElements = body.getBodyElements();
        if (body.getPartType() == BodyType.TABLECELL && bodyElements.size() == 1) {
            XWPFTableCell cell = (XWPFTableCell) body;
            Enum vMerge = this.stylesDocument.getTableCellVMerge(cell);
            if (vMerge != null && vMerge.equals(STMerge.CONTINUE)) {
                return false;
            } else {
                XWPFTableRow row = cell.getTableRow();
                List<XWPFTableCell> cells = row.getTableCells();
                Iterator i$ = cells.iterator();
                if (i$.hasNext()) {
                    XWPFTableCell c = (XWPFTableCell) i$.next();
                    if (c.getBodyElements().size() != 1) {
                        return false;
                    } else {
                        IBodyElement element = (IBodyElement) c.getBodyElements().get(0);
                        if (element.getElementType() != BodyElementType.PARAGRAPH) {
                            return false;
                        } else {
                            return ((XWPFParagraph) element).getRuns().size() == 0;
                        }
                    }
                } else {
                    return true;
                }
            }
        } else {
            return bodyElements.size() > index + 1;
        }
    }

    private void visitRuns(XWPFParagraph paragraph, T paragraphContainer) throws Exception {
        boolean fldCharTypeParsing = false;
        boolean pageNumber = false;
        String url = null;
        List<XmlObject> rListAfterSeparate = null;
        CTP ctp = paragraph.getCTP();
        XmlCursor c = ctp.newCursor();
        c.selectPath("child::*");

        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTR) {
                CTR r = (CTR) o;
                STFldCharType.Enum fldCharType = XWPFRunHelper.getFldCharType(r);
                if (fldCharType != null) {
                    if (fldCharType.equals(STFldCharType.BEGIN)) {
                        this.process(paragraph, paragraphContainer, pageNumber, url, rListAfterSeparate);
                        fldCharTypeParsing = true;
                        rListAfterSeparate = new ArrayList();
                        pageNumber = false;
                        url = null;
                    } else if (fldCharType.equals(STFldCharType.END)) {
                        this.process(paragraph, paragraphContainer, pageNumber, url, rListAfterSeparate);
                        fldCharTypeParsing = false;
                        rListAfterSeparate = null;
                        pageNumber = false;
                        this.processingTotalPageCountField = false;
                        url = null;
                    }
                } else if (fldCharTypeParsing) {
                    String instrText = XWPFRunHelper.getInstrText(r);
                    if (instrText != null) {
                        if (StringUtils.isNotEmpty(instrText)) {
                            boolean instrTextPage = XWPFRunHelper.isInstrTextPage(instrText);
                            if (!instrTextPage) {
                                this.processingTotalPageCountField = XWPFRunHelper.isInstrTextNumpages(instrText);
                                if (!this.totalPageFieldUsed) {
                                    this.totalPageFieldUsed = true;
                                }

                                String instrTextHyperlink = XWPFRunHelper.getInstrTextHyperlink(instrText);
                                if (instrTextHyperlink != null) {
                                    if (instrTextHyperlink.startsWith("\\l ")) {
                                        url = "#" + instrTextHyperlink.substring(3);
                                    } else {
                                        url = instrTextHyperlink;
                                    }
                                }
                            } else {
                                pageNumber = true;
                            }
                        }
                    } else {
                        rListAfterSeparate.add(r);
                    }
                } else {
                    XWPFRun run = new XWPFRun(r, paragraph);
                    this.visitRun(run, false, (String) null, paragraphContainer);
                }
            } else if (fldCharTypeParsing) {
                rListAfterSeparate.add(o);
            } else {
                this.visitRun(paragraph, o, paragraphContainer);
            }
        }

        c.dispose();
        this.process(paragraph, paragraphContainer, pageNumber, url, rListAfterSeparate);
        fldCharTypeParsing = false;
        rListAfterSeparate = null;
        pageNumber = false;
        url = null;
    }

    private void process(XWPFParagraph paragraph, T paragraphContainer, boolean pageNumber, String url, List<XmlObject> rListAfterSeparate) throws Exception {
        if (rListAfterSeparate != null) {
            Iterator i$ = rListAfterSeparate.iterator();

            while (i$.hasNext()) {
                XmlObject oAfterSeparate = (XmlObject) i$.next();
                if (oAfterSeparate instanceof CTR) {
                    CTR ctr = (CTR) oAfterSeparate;
                    XWPFRun run = new XWPFRun(ctr, paragraph);
                    this.visitRun(run, pageNumber, url, paragraphContainer);
                } else {
                    this.visitRun(paragraph, oAfterSeparate, paragraphContainer);
                }
            }
        }

    }

    private void visitRun(XWPFParagraph paragraph, XmlObject o, T paragraphContainer) throws Exception {
        String instr;
        String fieldHref;
        CTR r;
        Iterator i$;
        if (o instanceof CTHyperlink) {
            CTHyperlink link = (CTHyperlink) o;
            instr = link.getAnchor();
            String href = null;
            fieldHref = link.getId();
            if (StringUtils.isNotEmpty(fieldHref)) {
                XWPFHyperlink hyperlink = this.document.getHyperlinkByID(fieldHref);
                href = hyperlink != null ? hyperlink.getURL() : null;
            }

            i$ = link.getRList().iterator();

            while (i$.hasNext()) {
                r = (CTR) i$.next();
                XWPFRun run = new XWPFHyperlinkRun(link, r, paragraph);
                this.visitRun(run, false, href != null ? href : "#" + instr, paragraphContainer);
            }
        } else if (o instanceof CTSdtRun) {
            CTSdtContentRun run = ((CTSdtRun) o).getSdtContent();
            Iterator i$1 = run.getRList().iterator();

            while (i$1.hasNext()) {
                CTR r1 = (CTR) i$1.next();
                XWPFRun ru = new XWPFRun(r1, paragraph);
                this.visitRun(ru, false, (String) null, paragraphContainer);
            }
        } else if (o instanceof CTRunTrackChange) {
            Iterator i$1 = ((CTRunTrackChange) o).getRList().iterator();

            while (i$1.hasNext()) {
                CTR r1 = (CTR) i$1.next();
                XWPFRun run = new XWPFRun(r1, paragraph);
                this.visitRun(run, false, (String) null, paragraphContainer);
            }
        } else if (o instanceof CTSimpleField) {
            CTSimpleField simpleField = (CTSimpleField) o;
            instr = simpleField.getInstr();
            boolean fieldPageNumber = XWPFRunHelper.isInstrTextPage(instr);
            fieldHref = null;
            if (!fieldPageNumber) {
                fieldHref = XWPFRunHelper.getInstrTextHyperlink(instr);
            }

            i$ = simpleField.getRList().iterator();

            while (i$.hasNext()) {
                r = (CTR) i$.next();
                XWPFRun run = new XWPFRun(r, paragraph);
                this.visitRun(run, fieldPageNumber, fieldHref, paragraphContainer);
            }
        } else if (!(o instanceof CTSmartTagRun) && o instanceof CTBookmark) {
            CTBookmark bookmark = (CTBookmark) o;
            this.visitBookmark(bookmark, paragraph, paragraphContainer);
        }

    }

    protected abstract void visitEmptyRun(T var1) throws Exception;

    protected void visitRun(XWPFRun run, boolean pageNumber, String url, T paragraphContainer) throws Exception {
        CTR ctr = run.getCTR();
        CTRPr rPr = ctr.getRPr();
        boolean hasTexStyles = rPr != null && (rPr.getHighlight() != null || rPr.getStrike() != null || rPr.getDstrike() != null || rPr.getVertAlign() != null);
        StringBuilder text = new StringBuilder();
        XmlCursor c = ctr.newCursor();
        c.selectPath("./*");

        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTText) {
                CTText ctText = (CTText) o;
                String tagName = o.getDomNode().getNodeName();
                if (!"w:instrText".equals(tagName)) {
                    if (hasTexStyles) {
                        text.append(ctText.getStringValue());
                    } else {
                        this.visitText(ctText, pageNumber, paragraphContainer);
                    }
                }
            } else if (o instanceof CTPTab) {
                this.visitTab((CTPTab) o, paragraphContainer);
            } else if (o instanceof CTBr) {
                this.visitBR((CTBr) o, paragraphContainer);
            } else if (o instanceof CTEmpty) {
                String tagName = o.getDomNode().getNodeName();
                if ("w:tab".equals(tagName)) {
                    CTTabs tabs = this.stylesDocument.getParagraphTabs(run.getParagraph());
                    this.visitTabs(tabs, paragraphContainer);
                }

                if ("w:br".equals(tagName)) {
                    this.visitBR((CTBr) null, paragraphContainer);
                }

                if ("w:cr".equals(tagName)) {
                    this.visitBR((CTBr) null, paragraphContainer);
                }
            } else if (o instanceof CTDrawing) {
                this.visitDrawing((CTDrawing) o, paragraphContainer);
            }
        }

        if (hasTexStyles && StringUtils.isNotEmpty(text.toString())) {
            this.visitStyleText(run, text.toString());
        }

        c.dispose();
    }

    protected void visitStyleText(XWPFRun run, String text) throws Exception {
    }

    protected abstract void visitText(CTText var1, boolean var2, T var3) throws Exception;

    protected abstract void visitTab(CTPTab var1, T var2) throws Exception;

    protected abstract void visitTabs(CTTabs var1, T var2) throws Exception;

    protected void visitBR(CTBr br, T paragraphContainer) throws Exception {
        STBrType.Enum brType = XWPFRunHelper.getBrType(br);
        if (brType.equals(STBrType.PAGE)) {
            this.pageBreakOnNextParagraph = true;
        } else {
            this.addNewLine(br, paragraphContainer);
        }

    }

    protected abstract void visitBookmark(CTBookmark var1, XWPFParagraph var2, T var3) throws Exception;

    protected abstract void addNewLine(CTBr var1, T var2) throws Exception;

    protected abstract void pageBreak() throws Exception;

    protected void visitTable(XWPFTable table, int index, T container) throws Exception {
        float[] colWidths = XWPFTableUtil.computeColWidths(table);
        T tableContainer = this.startVisitTable(table, colWidths, container);
        this.visitTableBody(table, colWidths, tableContainer);
        this.endVisitTable(table, container, tableContainer);
    }

    protected void visitTableBody(XWPFTable table, float[] colWidths, T tableContainer) throws Exception {
        boolean firstRow = false;
        boolean lastRow = false;
        List<XWPFTableRow> rows = table.getRows();
        int rowsSize = rows.size();

        for (int i = 0; i < rowsSize; ++i) {
            firstRow = i == 0;
            lastRow = this.isLastRow(i, rowsSize);
            XWPFTableRow row = (XWPFTableRow) rows.get(i);
            this.visitTableRow(row, colWidths, tableContainer, firstRow, lastRow, i, rowsSize);
        }

    }

    private boolean isLastRow(int rowIndex, int rowsSize) {
        return rowIndex == rowsSize - 1;
    }

    protected abstract T startVisitTable(XWPFTable var1, float[] var2, T var3) throws Exception;

    protected abstract void endVisitTable(XWPFTable var1, T var2, T var3) throws Exception;

    protected void visitTableRow(XWPFTableRow row, float[] colWidths, T tableContainer, boolean firstRow, boolean lastRowIfNoneVMerge, int rowIndex, int rowsSize) throws Exception {
        boolean headerRow = this.stylesDocument.isTableRowHeader(row);
        this.startVisitTableRow(row, tableContainer, rowIndex, headerRow);
        int nbColumns = colWidths.length;
        boolean firstCol = true;
        boolean lastCol = false;
        boolean lastRow = false;
        List<XWPFTableCell> vMergedCells = null;
        List<XWPFTableCell> cells = row.getTableCells();
        int cellIndex;
        if (nbColumns > cells.size()) {
            firstCol = true;
            cellIndex = -1;
            int cellPtr = 0;
            CTRow ctRow = row.getCtRow();
            XmlCursor c = ctRow.newCursor();
            c.selectPath("./*");

            while (true) {
                while (c.toNextSelection()) {
                    XmlObject o = c.getObject();
                    if (o instanceof CTTc) {
                        CTTc tc = (CTTc) o;
                        XWPFTableCell cell = row.getTableCell(tc);
                        cellIndex = this.getCellIndex(cellIndex, cell);
                        lastCol = cellIndex == nbColumns;
                        vMergedCells = this.getVMergedCells(cell, rowIndex, cellPtr);
                        if (vMergedCells == null || vMergedCells.size() > 0) {
                            lastRow = this.isLastRow(lastRowIfNoneVMerge, rowIndex, rowsSize, vMergedCells);
                            this.visitCell(cell, tableContainer, firstRow, lastRow, firstCol, lastCol, rowIndex, cellPtr, vMergedCells);
                        }

                        ++cellPtr;
                        firstCol = false;
                    } else if (o instanceof CTSdtCell) {
                        CTSdtCell sdtCell = (CTSdtCell) o;
                        List<CTTc> tcList = sdtCell.getSdtContent().getTcList();

                        for (Iterator i$ = tcList.iterator(); i$.hasNext(); firstCol = false) {
                            CTTc ctTc = (CTTc) i$.next();
                            XWPFTableCell cell = new XWPFTableCell(ctTc, row, row.getTable().getBody());
                            cellIndex = this.getCellIndex(cellIndex, cell);
                            lastCol = cellIndex == nbColumns;
                            List<XWPFTableCell> rowCells = row.getTableCells();
                            if (!rowCells.contains(cell)) {
                                rowCells.add(cell);
                            }

                            vMergedCells = this.getVMergedCells(cell, rowIndex, cellPtr);
                            if (vMergedCells == null || vMergedCells.size() > 0) {
                                lastRow = this.isLastRow(lastRowIfNoneVMerge, rowIndex, rowsSize, vMergedCells);
                                this.visitCell(cell, tableContainer, firstRow, lastRow, firstCol, lastCol, rowIndex, cellPtr, vMergedCells);
                            }

                            ++cellPtr;
                        }
                    }
                }

                c.dispose();
                break;
            }
        } else {
            for (cellIndex = 0; cellIndex < cells.size(); ++cellIndex) {
                lastCol = cellIndex == cells.size() - 1;
                XWPFTableCell cell = (XWPFTableCell) cells.get(cellIndex);
                vMergedCells = this.getVMergedCells(cell, rowIndex, cellIndex);
                if (vMergedCells == null || vMergedCells.size() > 0) {
                    lastRow = this.isLastRow(lastRowIfNoneVMerge, rowIndex, rowsSize, vMergedCells);
                    this.visitCell(cell, tableContainer, firstRow, lastRow, firstCol, lastCol, rowIndex, cellIndex, vMergedCells);
                }

                firstCol = false;
            }
        }

        this.endVisitTableRow(row, tableContainer, firstRow, lastRow, headerRow);
    }

    private boolean isLastRow(boolean lastRowIfNoneVMerge, int rowIndex, int rowsSize, List<XWPFTableCell> vMergedCells) {
        return vMergedCells == null ? lastRowIfNoneVMerge : this.isLastRow(rowIndex - 1 + vMergedCells.size(), rowsSize);
    }

    private int getCellIndex(int cellIndex, XWPFTableCell cell) {
        BigInteger gridSpan = this.stylesDocument.getTableCellGridSpan(cell.getCTTc().getTcPr());
        if (gridSpan != null) {
            cellIndex += gridSpan.intValue();
        } else {
            ++cellIndex;
        }

        return cellIndex;
    }

    protected void startVisitTableRow(XWPFTableRow row, T tableContainer, int rowIndex, boolean headerRow) throws Exception {
    }

    protected void endVisitTableRow(XWPFTableRow row, T tableContainer, boolean firstRow, boolean lastRow, boolean headerRow) throws Exception {
    }

    protected void visitCell(XWPFTableCell cell, T tableContainer, boolean firstRow, boolean lastRow, boolean firstCol, boolean lastCol, int rowIndex, int cellIndex, List<XWPFTableCell> vMergedCells) throws Exception {
        T tableCellContainer = this.startVisitTableCell(cell, tableContainer, firstRow, lastRow, firstCol, lastCol, vMergedCells);
        this.visitTableCellBody(cell, vMergedCells, tableCellContainer);
        this.endVisitTableCell(cell, tableContainer, tableCellContainer);
    }

    private List<XWPFTableCell> getVMergedCells(XWPFTableCell cell, int rowIndex, int cellIndex) {
        List<XWPFTableCell> vMergedCells = null;
        Enum vMerge = this.stylesDocument.getTableCellVMerge(cell);
        if (vMerge != null) {
            if (!vMerge.equals(STMerge.RESTART)) {
                return Collections.emptyList();
            }

            vMergedCells = new ArrayList();
            vMergedCells.add(cell);
            XWPFTableRow row = null;
            XWPFTable table = cell.getTableRow().getTable();

            for (int i = rowIndex + 1; i < table.getRows().size(); ++i) {
                row = table.getRow(i);
                XWPFTableCell c = row.getCell(cellIndex);
                if (c == null) {
                    break;
                }

                vMerge = this.stylesDocument.getTableCellVMerge(c);
                if (vMerge == null || !vMerge.equals(STMerge.CONTINUE)) {
                    return vMergedCells;
                }

                vMergedCells.add(c);
            }
        }

        return vMergedCells;
    }

    protected void visitTableCellBody(XWPFTableCell cell, List<XWPFTableCell> vMergeCells, T tableCellContainer) throws Exception {
        if (vMergeCells != null) {
            Iterator i$ = vMergeCells.iterator();

            while (i$.hasNext()) {
                XWPFTableCell mergedCell = (XWPFTableCell) i$.next();
                List<IBodyElement> bodyElements = mergedCell.getBodyElements();
                this.visitBodyElements(bodyElements, tableCellContainer);
            }
        } else {
            List<IBodyElement> bodyElements = cell.getBodyElements();
            this.visitBodyElements(bodyElements, tableCellContainer);
        }

    }

    protected abstract T startVisitTableCell(XWPFTableCell var1, T var2, boolean var3, boolean var4, boolean var5, boolean var6, List<XWPFTableCell> var7) throws Exception;

    protected abstract void endVisitTableCell(XWPFTableCell var1, T var2, T var3) throws Exception;

    protected XWPFStyle getXWPFStyle(String styleID) {
        return styleID == null ? null : this.document.getStyles().getStyle(styleID);
    }

    protected boolean isWordDocumentPartParsing() {
        return this.currentHeader == null && this.currentFooter == null;
    }

    @Override
    public void visitHeaderRef(CTHdrFtrRef headerRef, CTSectPr sectPr, E masterPage) throws Exception {
        this.currentHeader = this.getXWPFHeader(headerRef);
        this.visitHeader(this.currentHeader, headerRef, sectPr, masterPage);
        this.currentHeader = null;
    }

    protected abstract void visitHeader(XWPFHeader var1, CTHdrFtrRef var2, CTSectPr var3, E var4) throws Exception;

    @Override
    public void visitFooterRef(CTHdrFtrRef footerRef, CTSectPr sectPr, E masterPage) throws Exception {
        this.currentFooter = this.getXWPFFooter(footerRef);
        this.visitFooter(this.currentFooter, footerRef, sectPr, masterPage);
        this.currentFooter = null;
    }

    protected abstract void visitFooter(XWPFFooter var1, CTHdrFtrRef var2, CTSectPr var3, E var4) throws Exception;

    protected List<IBodyElement> getBodyElements(XWPFHeaderFooter part) {
        List<IBodyElement> bodyElements = new ArrayList();
        XmlTokenSource headerFooter = part._getHdrFtr();
        this.addBodyElements(headerFooter, part, bodyElements);
        return bodyElements;
    }

    private void addBodyElements(XmlTokenSource source, IBody part, List<IBodyElement> bodyElements) {
        XmlCursor cursor = source.newCursor();
        cursor.selectPath("./*");

        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTSdtBlock) {
                CTSdtBlock block = (CTSdtBlock) o;
                CTSdtContentBlock contentBlock = block.getSdtContent();
                if (contentBlock != null) {
                    this.addBodyElements(contentBlock, part, bodyElements);
                }
            } else if (o instanceof CTP) {
                XWPFParagraph p = new XWPFParagraph((CTP) o, part);
                bodyElements.add(p);
            } else if (o instanceof CTTbl) {
                XWPFTable t = new XWPFTable((CTTbl) o, part);
                bodyElements.add(t);
            }
        }

        cursor.dispose();
    }

    protected XWPFHeader getXWPFHeader(CTHdrFtrRef headerRef) throws XmlException, IOException {
        PackagePart hdrPart = this.document.getPartById(headerRef.getId());
        List<XWPFHeader> headers = this.document.getHeaderList();
        Iterator i$ = headers.iterator();

        XWPFHeader header;
        do {
            if (!i$.hasNext()) {
                HdrDocument hdrDoc = Factory.parse(hdrPart.getInputStream());
                CTHdrFtr hdrFtr = hdrDoc.getHdr();
                XWPFHeader hdr = new XWPFHeader(this.document, hdrFtr);
                return hdr;
            }

            header = (XWPFHeader) i$.next();
        } while (!header.getPackagePart().equals(hdrPart));

        return header;
    }

    protected XWPFFooter getXWPFFooter(CTHdrFtrRef footerRef) throws XmlException, IOException {
        PackagePart hdrPart = this.document.getPartById(footerRef.getId());
        List<XWPFFooter> footers = this.document.getFooterList();
        Iterator i$ = footers.iterator();

        XWPFFooter footer;
        do {
            if (!i$.hasNext()) {
                FtrDocument hdrDoc = FtrDocument.Factory.parse(hdrPart.getInputStream());
                CTHdrFtr hdrFtr = hdrDoc.getFtr();
                XWPFFooter ftr = new XWPFFooter(this.document, hdrFtr);
                return ftr;
            }

            footer = (XWPFFooter) i$.next();
        } while (!footer.getPackagePart().equals(hdrPart));

        return footer;
    }

    protected void visitDrawing(CTDrawing drawing, T parentContainer) throws Exception {
        List<CTInline> inlines = drawing.getInlineList();
        Iterator i$ = inlines.iterator();

        while (i$.hasNext()) {
            CTInline inline = (CTInline) i$.next();
            this.visitInline(inline, parentContainer);
        }

        List<CTAnchor> anchors = drawing.getAnchorList();
        Iterator i$1 = anchors.iterator();

        while (i$1.hasNext()) {
            CTAnchor anchor = (CTAnchor) i$1.next();
            this.visitAnchor(anchor, parentContainer);
        }

    }

    protected void visitAnchor(CTAnchor anchor, T parentContainer) throws Exception {
        CTGraphicalObject graphic = anchor.getGraphic();
        STRelFromH.Enum relativeFromH = null;
        Float offsetX = null;
        CTPosH positionH = anchor.getPositionH();
        if (positionH != null) {
            relativeFromH = positionH.getRelativeFrom();
            offsetX = DxaUtil.emu2points((long) positionH.getPosOffset());
        }

        STRelFromV.Enum relativeFromV = null;
        Float offsetY = null;
        CTPosV positionV = anchor.getPositionV();
        if (positionV != null) {
            relativeFromV = positionV.getRelativeFrom();
            offsetY = DxaUtil.emu2points((long) positionV.getPosOffset());
        }

        STWrapText.Enum wrapText = null;
        CTWrapSquare wrapSquare = anchor.getWrapSquare();
        if (wrapSquare != null) {
            wrapText = wrapSquare.getWrapText();
        }

        this.visitGraphicalObject(parentContainer, graphic, offsetX, relativeFromH, offsetY, relativeFromV, wrapText);
    }

    protected void visitInline(CTInline inline, T parentContainer) throws Exception {
        CTGraphicalObject graphic = inline.getGraphic();
        this.visitGraphicalObject(parentContainer, graphic, (Float) null, (STRelFromH.Enum) null, (Float) null, (STRelFromV.Enum) null, (STWrapText.Enum) null);
    }

    private void visitGraphicalObject(T parentContainer, CTGraphicalObject graphic, Float offsetX, STRelFromH.Enum relativeFromH, Float offsetY, STRelFromV.Enum relativeFromV, STWrapText.Enum wrapText) throws Exception {
        if (graphic != null) {
            CTGraphicalObjectData graphicData = graphic.getGraphicData();
            if (graphicData != null) {
                XmlCursor c = graphicData.newCursor();
                c.selectPath("./*");

                while (c.toNextSelection()) {
                    XmlObject o = c.getObject();
                    if (o instanceof CTPicture) {
                        CTPicture picture = (CTPicture) o;
                        IImageExtractor extractor = this.getImageExtractor();
                        if (extractor != null) {
                            XWPFPictureData pictureData = this.getPictureData(picture);
                            if (pictureData != null) {
                                try {
                                    extractor.extract("word/media/" + pictureData.getFileName(), pictureData.getData());
                                } catch (Throwable var15) {
                                    LOGGER.log(Level.SEVERE, "Error while extracting the image " + pictureData.getFileName(), var15);
                                }
                            }
                        }

                        this.visitPicture(picture, offsetX, relativeFromH, offsetY, relativeFromV, wrapText, parentContainer);
                    }
                }

                c.dispose();
            }
        }

    }

    protected XWPFPictureData getPictureDataByID(String blipId) {
        if (this.currentHeader != null) {
            return this.currentHeader.getPictureDataByID(blipId);
        } else {
            return this.currentFooter != null ? this.currentFooter.getPictureDataByID(blipId) : this.document.getPictureDataByID(blipId);
        }
    }

    protected IImageExtractor getImageExtractor() {
        return this.options.getExtractor();
    }

    public XWPFPictureData getPictureData(CTPicture picture) {
        String blipId = picture.getBlipFill().getBlip().getEmbed();
        return this.getPictureDataByID(blipId);
    }

    protected abstract void visitPicture(CTPicture var1, Float var2, STRelFromH.Enum var3, Float var4, STRelFromV.Enum var5, STWrapText.Enum var6, T var7) throws Exception;
}
