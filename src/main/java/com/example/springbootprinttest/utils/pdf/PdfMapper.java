//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.springbootprinttest.utils.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.DottedLineSeparator;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.pdf.draw.VerticalPositionMark;
import fr.opensagres.poi.xwpf.converter.core.*;
import fr.opensagres.poi.xwpf.converter.core.styles.paragraph.ParagraphIndentationHangingValueProvider;
import fr.opensagres.poi.xwpf.converter.core.styles.paragraph.ParagraphIndentationLeftValueProvider;
import fr.opensagres.poi.xwpf.converter.core.utils.DxaUtil;
import fr.opensagres.poi.xwpf.converter.core.utils.StringUtils;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.poi.xwpf.converter.pdf.internal.Converter;
import fr.opensagres.poi.xwpf.converter.pdf.internal.elements.*;
import fr.opensagres.xdocreport.itext.extension.*;
import fr.opensagres.xdocreport.itext.extension.font.FontGroup;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromH;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromV;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabTlc.Enum;

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class PdfMapper extends XWPFDocumentVisitor<IITextContainer, PdfOptions, StylableMasterPage> {
    private static final String TAB = "\t";
    private static final Logger LOGGER = Logger.getLogger(PdfMapper.class.getName());
    private final OutputStream out;
    private StylableDocument pdfDocument;
    private Font currentRunFontAscii;
    private Font currentRunFontEastAsia;
    private Font currentRunFontHAnsi;
    private UnderlinePatterns currentRunUnderlinePatterns;
    private Color currentRunBackgroundColor;
    private Float currentRunX;
    private Float currentPageWidth;
    private StylableHeaderFooter pdfHeader;
    private StylableHeaderFooter pdfFooter;
    private Integer expectedPageCount;

    public PdfMapper(XWPFDocument document, OutputStream out, PdfOptions options, Integer expectedPageCount) throws Exception {
        super(document, options != null ? options : PdfOptions.getDefault());
        this.out = out;
        this.expectedPageCount = expectedPageCount;
    }

    @Override
    protected IITextContainer startVisitDocument() throws Exception {
        this.pdfDocument = new StylableDocument(this.out, ((PdfOptions)this.options).getConfiguration());
        this.pdfDocument.setMasterPageManager(this.getMasterPageManager());
        return this.pdfDocument;
    }

    @Override
    protected void endVisitDocument() throws Exception {
        this.pdfDocument.close();
        this.out.close();
    }

    @Override
    protected IITextContainer startVisitSDT(XWPFSDT contents, IITextContainer container) {
        return null;
    }

    @Override
    protected void endVisitSDT(XWPFSDT contents, IITextContainer container, IITextContainer sdtContainer) {
    }

    @Override
    protected void visitSDTBody(XWPFSDT contents, IITextContainer sdtContainer) throws Exception {
    }

    @Override
    protected void visitHeader(XWPFHeader header, CTHdrFtrRef headerRef, CTSectPr sectPr, StylableMasterPage masterPage) throws Exception {
        BigInteger headerY = sectPr.getPgMar() != null ? sectPr.getPgMar().getHeader() : null;
        this.currentPageWidth = sectPr.getPgMar() != null ? DxaUtil.dxa2points(sectPr.getPgSz().getW()) : null;
        this.pdfHeader = new StylableHeaderFooter(this.pdfDocument, headerY, true);
        List<IBodyElement> bodyElements = super.getBodyElements(header);
        StylableTableCell tableCell = this.getHeaderFooterTableCell(this.pdfHeader, bodyElements);
        this.visitBodyElements(bodyElements, tableCell);
        masterPage.setHeader(this.pdfHeader);
        this.currentPageWidth = null;
        this.pdfHeader = null;
    }

    @Override
    protected void visitFooter(XWPFFooter footer, CTHdrFtrRef footerRef, CTSectPr sectPr, StylableMasterPage masterPage) throws Exception {
        BigInteger footerY = sectPr.getPgMar() != null ? sectPr.getPgMar().getFooter() : null;
        this.currentPageWidth = sectPr.getPgMar() != null ? DxaUtil.dxa2points(sectPr.getPgSz().getW()) : null;
        this.pdfFooter = new StylableHeaderFooter(this.pdfDocument, footerY, false);
        List<IBodyElement> bodyElements = super.getBodyElements(footer);
        StylableTableCell tableCell = this.getHeaderFooterTableCell(this.pdfFooter, bodyElements);
        this.visitBodyElements(bodyElements, tableCell);
        masterPage.setFooter(this.pdfFooter);
        this.currentPageWidth = null;
        this.pdfFooter = null;
    }

    private StylableTableCell getHeaderFooterTableCell(StylableHeaderFooter pdfHeaderFooter, List<IBodyElement> bodyElements) throws DocumentException {
        return pdfHeaderFooter.getTableCell();
    }

    public void setActiveMasterPage(StylableMasterPage masterPage) {
        this.pdfDocument.setActiveMasterPage(masterPage);
    }

    public StylableMasterPage createMasterPage(CTSectPr sectPr) {
        return new StylableMasterPage(sectPr);
    }

    @Override
    protected IITextContainer startVisitParagraph(XWPFParagraph docxParagraph, ListItemContext itemContext, IITextContainer pdfParentContainer) throws Exception {
        this.currentRunX = null;
        StylableParagraph pdfParagraph = this.pdfDocument.createParagraph(pdfParentContainer);
        Float indentationLeft = this.stylesDocument.getIndentationLeft(docxParagraph);
        if (indentationLeft != null) {
            pdfParagraph.setIndentationLeft(indentationLeft);
        }

        Float indentationRight = this.stylesDocument.getIndentationRight(docxParagraph);
        if (indentationRight != null) {
            pdfParagraph.setIndentationRight(indentationRight);
        }

        Float indentationFirstLine = this.stylesDocument.getIndentationFirstLine(docxParagraph);
        if (indentationFirstLine != null) {
            pdfParagraph.setFirstLineIndent(indentationFirstLine);
        }

        Float indentationHanging = this.stylesDocument.getIndentationHanging(docxParagraph);
        if (indentationHanging != null) {
            pdfParagraph.setFirstLineIndent(-indentationHanging);
        }

        Float spacingBefore = this.stylesDocument.getSpacingBefore(docxParagraph);
        if (spacingBefore != null) {
            pdfParagraph.setSpacingBefore(spacingBefore);
        }

        Float spacingAfter = this.stylesDocument.getSpacingAfter(docxParagraph);
        if (spacingAfter != null) {
            pdfParagraph.setSpacingAfter(spacingAfter);
        }

        ParagraphLineSpacing lineSpacing = this.stylesDocument.getParagraphSpacing(docxParagraph);
        if (lineSpacing != null) {
            if (lineSpacing.getLeading() != null && lineSpacing.getMultipleLeading() != null) {
                pdfParagraph.setLeading(lineSpacing.getLeading(), lineSpacing.getMultipleLeading());
            } else {
                if (lineSpacing.getLeading() != null) {
                    pdfParagraph.setLeading(lineSpacing.getLeading());
                }

                if (lineSpacing.getMultipleLeading() != null) {
                    pdfParagraph.setMultipliedLeading(lineSpacing.getMultipleLeading());
                }
            }
        }

        ParagraphAlignment alignment = this.stylesDocument.getParagraphAlignment(docxParagraph);
        if (alignment != null) {
            switch(alignment) {
            case LEFT:
                pdfParagraph.setAlignment(0);
                break;
            case RIGHT:
                pdfParagraph.setAlignment(2);
                break;
            case CENTER:
                pdfParagraph.setAlignment(1);
                break;
            case BOTH:
                pdfParagraph.setAlignment(3);
            }
        }

        Color backgroundColor = this.stylesDocument.getBackgroundColor(docxParagraph);
        if (backgroundColor != null) {
            pdfParagraph.setBackgroundColor(Converter.toAwtColor(backgroundColor));
        }

        CTBorder borderTop = this.stylesDocument.getBorderTop(docxParagraph);
        pdfParagraph.setBorder(borderTop, 1);
        CTBorder borderBottom = this.stylesDocument.getBorderBottom(docxParagraph);
        pdfParagraph.setBorder(borderBottom, 2);
        CTBorder borderLeft = this.stylesDocument.getBorderLeft(docxParagraph);
        pdfParagraph.setBorder(borderLeft, 4);
        CTBorder borderRight = this.stylesDocument.getBorderRight(docxParagraph);
        pdfParagraph.setBorder(borderRight, 8);
        if (itemContext != null) {
            CTLvl lvl = itemContext.getLvl();
            CTPPr lvlPPr = lvl.getPPr();
            if (lvlPPr != null) {
                Float hanging;
                if (ParagraphIndentationLeftValueProvider.INSTANCE.getValue(docxParagraph.getCTP().getPPr()) == null) {
                    hanging = (Float)ParagraphIndentationLeftValueProvider.INSTANCE.getValue(lvlPPr);
                    if (hanging != null) {
                        pdfParagraph.setIndentationLeft(hanging);
                    }
                }

                if (ParagraphIndentationHangingValueProvider.INSTANCE.getValue(docxParagraph.getCTP().getPPr()) == null) {
                    hanging = this.stylesDocument.getIndentationHanging(lvlPPr);
                    if (hanging != null) {
                        pdfParagraph.setFirstLineIndent(-hanging);
                    }
                }
            }

            CTRPr lvlRPr = lvl.getRPr();
            if (lvlRPr != null) {
                String listItemFontFamily = this.stylesDocument.getFontFamilyAscii(lvlRPr);
                Float listItemFontSize = this.stylesDocument.getFontSize(lvlRPr);
                int listItemFontStyle = 0;
                Boolean bold = this.stylesDocument.getFontStyleBold(lvlRPr);
                if (bold != null && bold) {
                    listItemFontStyle |= 1;
                }

                Boolean italic = this.stylesDocument.getFontStyleItalic(lvlRPr);
                if (italic != null && italic) {
                    listItemFontStyle |= 2;
                }

                Boolean strike = this.stylesDocument.getFontStyleStrike(lvlRPr);
                if (strike != null && strike) {
                    listItemFontStyle |= 8;
                }

                Color listItemFontColor = this.stylesDocument.getFontColor(lvlRPr);
                pdfParagraph.setListItemFontFamily(listItemFontFamily);
                pdfParagraph.setListItemFontSize(listItemFontSize);
                pdfParagraph.setListItemFontStyle(listItemFontStyle);
                pdfParagraph.setListItemFontColor(Converter.toAwtColor(listItemFontColor));
            }

            pdfParagraph.setListItemText(itemContext.getText());
        }

        return pdfParagraph;
    }

    @Override
    protected void endVisitParagraph(XWPFParagraph docxParagraph, IITextContainer pdfParentContainer, IITextContainer pdfParagraphContainer) throws Exception {
        ExtendedParagraph pdfParagraph = (ExtendedParagraph)pdfParagraphContainer;
        pdfParentContainer.addElement(pdfParagraph.getElement());
        this.currentRunX = null;
    }

    @Override
    protected void visitEmptyRun(IITextContainer pdfParagraphContainer) throws Exception {
        StylableParagraph paragraph = (StylableParagraph)pdfParagraphContainer;
        IITextContainer parent = paragraph.getParent();
        if (parent instanceof StylableTableCell) {
            StylableTableCell cell = (StylableTableCell)parent;
            if (cell.getRotation() > 0) {
                return;
            }
        }

        pdfParagraphContainer.addElement(Chunk.NEWLINE);
    }

    @Override
    protected void visitRun(XWPFRun docxRun, boolean pageNumber, String url, IITextContainer pdfParagraphContainer) throws Exception {
        String fontFamilyAscii = this.stylesDocument.getFontFamilyAscii(docxRun);
        String fontFamilyEastAsia = this.stylesDocument.getFontFamilyEastAsia(docxRun);
        String fontFamilyHAnsi = this.stylesDocument.getFontFamilyHAnsi(docxRun);
        Float fontSize = this.stylesDocument.getFontSize(docxRun);
        if (fontSize == null) {
            fontSize = -1.0F;
        }

        int fontStyle = 0;
        Boolean bold = this.stylesDocument.getFontStyleBold(docxRun);
        if (bold != null && bold) {
            fontStyle |= 1;
        }

        Boolean italic = this.stylesDocument.getFontStyleItalic(docxRun);
        if (italic != null && italic) {
            fontStyle |= 2;
        }

        Boolean strike = this.stylesDocument.getFontStyleStrike(docxRun);
        if (strike != null && strike) {
            fontStyle |= 8;
        }

        Color fontColor = this.stylesDocument.getFontColor(docxRun);
        this.currentRunFontAscii = this.getFont(fontFamilyAscii, fontSize, fontStyle, fontColor);
        this.currentRunFontEastAsia = this.getFont(fontFamilyEastAsia, fontSize, fontStyle, fontColor);
        this.currentRunFontHAnsi = this.getFont(fontFamilyHAnsi, fontSize, fontStyle, fontColor);
        this.currentRunUnderlinePatterns = this.stylesDocument.getUnderline(docxRun);
        this.currentRunBackgroundColor = this.stylesDocument.getBackgroundColor(docxRun);
        if (this.currentRunBackgroundColor == null) {
            this.currentRunBackgroundColor = this.stylesDocument.getTextHighlighting(docxRun);
        }

        StylableParagraph pdfParagraph = (StylableParagraph)pdfParagraphContainer;
        pdfParagraph.adjustMultipliedLeading(this.currentRunFontAscii);
        String listItemText = pdfParagraph.getListItemText();
        if (StringUtils.isNotEmpty(listItemText)) {
            listItemText = listItemText + "    ";
            String listItemFontFamily = pdfParagraph.getListItemFontFamily();
            Float listItemFontSize = pdfParagraph.getListItemFontSize();
            int listItemFontStyle = pdfParagraph.getListItemFontStyle();
            java.awt.Color listItemFontColor = pdfParagraph.getListItemFontColor();
            Font listItemFont = ((PdfOptions)this.options).getFontProvider().getFont(listItemFontFamily != null ? listItemFontFamily : fontFamilyAscii, ((PdfOptions)this.options).getFontEncoding(), listItemFontSize != null ? listItemFontSize : fontSize, listItemFontStyle != 0 ? listItemFontStyle : fontStyle, listItemFontColor != null ? listItemFontColor : Converter.toAwtColor(fontColor));
            Chunk symbol = this.createTextChunk(listItemText, false, listItemFont, this.currentRunUnderlinePatterns, this.currentRunBackgroundColor);
            pdfParagraph.add(symbol);
            pdfParagraph.setListItemText((String)null);
        }

        IITextContainer container = pdfParagraphContainer;
        if (url != null) {
            StylableAnchor pdfAnchor = new StylableAnchor();
            pdfAnchor.setReference(url);
            pdfAnchor.setITextContainer(pdfParagraphContainer);
            container = pdfAnchor;
        }

        super.visitRun(docxRun, pageNumber, url, container);
        if (url != null) {
            pdfParagraphContainer.addElement((StylableAnchor)container);
        }

        this.currentRunFontAscii = null;
        this.currentRunFontEastAsia = null;
        this.currentRunFontHAnsi = null;
        this.currentRunUnderlinePatterns = null;
        this.currentRunBackgroundColor = null;
    }

    private Font getFont(String fontFamily, Float fontSize, int fontStyle, Color fontColor) {
        String fontToUse = this.stylesDocument.getFontNameToUse(fontFamily);
        if (StringUtils.isNotEmpty(fontToUse)) {
            return ((PdfOptions)this.options).getFontProvider().getFont(fontToUse, ((PdfOptions)this.options).getFontEncoding(), fontSize, fontStyle, Converter.toAwtColor(fontColor));
        } else {
            Font font = ((PdfOptions)this.options).getFontProvider().getFont(fontFamily, ((PdfOptions)this.options).getFontEncoding(), fontSize, fontStyle, Converter.toAwtColor(fontColor));
            if (!this.isFontExists(font)) {
                try {
                    List<String> altNames = this.stylesDocument.getFontsAltName(fontFamily);
                    if (altNames != null) {
                        Iterator i$ = altNames.iterator();

                        while(i$.hasNext()) {
                            String altName = (String)i$.next();
                            if (!fontFamily.equals(altName)) {
                                font = this.getFont(altName, fontSize, fontStyle, fontColor);
                                if (this.isFontExists(font)) {
                                    this.stylesDocument.setFontNameToUse(fontFamily, altName);
                                    return font;
                                }
                            }
                        }
                    }
                } catch (Exception var10) {
                    LOGGER.severe(var10.getMessage());
                }
            }

            return font;
        }
    }

    private boolean isFontExists(Font font) {
        return font != null && font.getBaseFont() != null;
    }

    @Override
    protected void visitText(CTText docxText, boolean pageNumber, IITextContainer pdfParagraphContainer) throws Exception {
        Font font = this.currentRunFontAscii;
        Font fontAsian = this.currentRunFontEastAsia;
        Font fontComplex = this.currentRunFontHAnsi;
        this.createAndAddChunks(pdfParagraphContainer, docxText.getStringValue(), this.currentRunUnderlinePatterns, this.currentRunBackgroundColor, pageNumber, font, fontAsian, fontComplex);
    }

    private Chunk createTextChunk(String text, boolean pageNumber, Font currentRunFont, UnderlinePatterns currentRunUnderlinePatterns, Color currentRunBackgroundColor) {
        Chunk textChunk = null;
        if (this.processingTotalPageCountField && this.expectedPageCount != null) {
            textChunk = new Chunk(String.valueOf(this.expectedPageCount), currentRunFont);
        } else {
            textChunk = pageNumber ? new ExtendedChunk(this.pdfDocument, true, currentRunFont) : new Chunk(text, currentRunFont);
        }

        if (currentRunUnderlinePatterns != null) {
            boolean singleUnderlined = false;
            switch(currentRunUnderlinePatterns) {
            case SINGLE:
                singleUnderlined = true;
            default:
                if (singleUnderlined) {
                    ((Chunk)textChunk).setUnderline(0.2F, -2.0F);
                }
            }
        }

        if (currentRunBackgroundColor != null) {
            ((Chunk)textChunk).setBackground(Converter.toAwtColor(currentRunBackgroundColor));
        }

        if (this.currentRunX != null) {
            this.currentRunX = this.currentRunX + ((Chunk)textChunk).getWidthPoint();
        }

        return (Chunk)textChunk;
    }

    private void createAndAddChunks(IITextContainer parent, String textContent, UnderlinePatterns underlinePatterns, Color backgroundColor, boolean pageNumber, Font font, Font fontAsian, Font fontComplex) {
        StringBuilder sbuf = new StringBuilder();
        FontGroup currentGroup = FontGroup.WESTERN;

        for(int i = 0; i < textContent.length(); ++i) {
            char ch = textContent.charAt(i);
            FontGroup group = FontGroup.getUnicodeGroup(ch, font, fontAsian, fontComplex);
            if (sbuf.length() != 0 && !currentGroup.equals(group)) {
                Font chunkFont = this.getFont(font, fontAsian, fontComplex, currentGroup);
                Chunk chunk = this.createTextChunk(sbuf.toString(), pageNumber, chunkFont, underlinePatterns, backgroundColor);
                parent.addElement(chunk);
                sbuf.setLength(0);
                sbuf.append(ch);
            } else {
                sbuf.append(ch);
            }

            currentGroup = group;
        }

        Font chunkFont = this.getFont(font, fontAsian, fontComplex, currentGroup);
        Chunk chunk = this.createTextChunk(sbuf.toString(), pageNumber, chunkFont, underlinePatterns, backgroundColor);
        parent.addElement(chunk);
    }

    private Font getFont(Font font, Font fontAsian, Font fontComplex, FontGroup group) {
        switch(group) {
        case WESTERN:
            return font;
        case ASIAN:
            return fontAsian;
        case COMPLEX:
            return fontComplex;
        default:
            return font;
        }
    }

    @Override
    protected void visitTab(CTPTab tab, IITextContainer pdfParagraphContainer) throws Exception {
    }

    @Override
    protected void visitTabs(CTTabs tabs, IITextContainer pdfParagraphContainer) throws Exception {
        if (this.currentRunX == null) {
            Paragraph paragraph = null;
            if (pdfParagraphContainer instanceof Paragraph) {
                paragraph = (Paragraph)pdfParagraphContainer;
            } else {
                paragraph = (Paragraph)((StylableAnchor)pdfParagraphContainer).getITextContainer();
            }

            this.currentRunX = paragraph.getFirstLineIndent();
            List<Chunk> chunks = paragraph.getChunks();

            Chunk chunk;
            for(Iterator i$ = chunks.iterator(); i$.hasNext(); this.currentRunX = this.currentRunX + chunk.getWidthPoint()) {
                chunk = (Chunk)i$.next();
            }
        } else if (this.currentRunX >= this.pdfDocument.getPageWidth()) {
            this.currentRunX = 0.0F;
        }

        Float tabPosition = null;
        Enum tabLeader = null;
        STTabJc.Enum tabVal = null;
        boolean useDefaultTabStop = false;
        CTTabStop pdfTab;
        if (tabs != null) {
            List<CTTabStop> tabList = tabs.getTabList();
            pdfTab = this.getTabStop(tabList);
            if (pdfTab != null) {
                float lastX = DxaUtil.dxa2points(pdfTab.getPos().floatValue());
                if (lastX > this.currentRunX) {
                    tabPosition = lastX;
                    tabLeader = pdfTab.getLeader();
                    tabVal = pdfTab.getVal();
                } else {
                    useDefaultTabStop = true;
                }
            }
        }

        if (tabs == null || useDefaultTabStop) {
            float defaultTabStop = this.stylesDocument.getDefaultTabStop();
            float pageWidth = this.pdfDocument.getPageWidth();
            int nbInterval = (int)(pageWidth / defaultTabStop);
            Float lastX = this.getTabStopPosition(this.currentRunX, defaultTabStop, nbInterval);
            if (lastX != null) {
                tabPosition = lastX;
            }
        }

        if (tabPosition != null) {
            this.currentRunX = tabPosition;
            VerticalPositionMark mark = this.createVerticalPositionMark(tabLeader);
            pdfTab = null;
            Chunk pdfTab1;
            if (STTabJc.RIGHT.equals(tabVal)) {
                pdfTab1 = new Chunk(mark);
            } else {
                pdfTab1 = new Chunk(mark, this.currentRunX);
            }

            pdfParagraphContainer.addElement(pdfTab1);
        }

    }

    private Float getTabStopPosition(float currentPosition, float interval, int nbInterval) {
        Float nextPosition = null;
        float newPosition = 0.0F;

        for(int i = 1; i < nbInterval; ++i) {
            newPosition = interval * (float)i;
            if (currentPosition < newPosition) {
                nextPosition = newPosition;
                break;
            }
        }

        return nextPosition;
    }

    private VerticalPositionMark createVerticalPositionMark(Enum leader) {
        if (leader != null) {
            if (leader == STTabTlc.DOT) {
                return new DottedLineSeparator();
            }

            if (leader == STTabTlc.UNDERSCORE) {
                return new LineSeparator();
            }
        }

        return new VerticalPositionMark();
    }

    private CTTabStop getTabStop(List<CTTabStop> tabList) {
        CTTabStop selectedTabStop;
        if (tabList.size() == 1) {
            selectedTabStop = (CTTabStop)tabList.get(0);
            return this.isClearTab(selectedTabStop) ? null : selectedTabStop;
        } else {
            selectedTabStop = null;
            Iterator i$ = tabList.iterator();

            CTTabStop tabStop;
            do {
                if (!i$.hasNext()) {
                    return null;
                }

                tabStop = (CTTabStop)i$.next();
            } while(this.isClearTab(tabStop) || !this.canApplyTabStop(tabStop));

            return tabStop;
        }
    }

    private boolean canApplyTabStop(CTTabStop tabStop) {
        if (tabStop.getVal().equals(STTabJc.LEFT)) {
            if (this.currentRunX < DxaUtil.dxa2points(tabStop.getPos().floatValue())) {
                return true;
            }
        } else if (tabStop.getVal().equals(STTabJc.RIGHT)) {
            if (this.isWordDocumentPartParsing()) {
                if (this.pdfDocument.getWidthLimit() - (this.currentRunX + DxaUtil.dxa2points(tabStop.getPos().floatValue())) <= 0.0F) {
                    return true;
                }
            } else {
                if (this.currentPageWidth == null) {
                    return true;
                }

                if (this.currentPageWidth - (this.currentRunX + DxaUtil.dxa2points(tabStop.getPos().floatValue())) <= 0.0F) {
                    return true;
                }
            }
        } else if (tabStop.getVal().equals(STTabJc.CENTER)) {
        }

        return false;
    }

    private boolean isClearTab(CTTabStop tabStop) {
        STTabJc.Enum tabVal = tabStop.getVal();
        return tabVal != null && tabVal.equals(STTabJc.CLEAR);
    }

    @Override
    protected void addNewLine(CTBr br, IITextContainer pdfParagraphContainer) throws Exception {
        pdfParagraphContainer.addElement(Chunk.NEWLINE);
    }

    @Override
    protected void visitBR(CTBr br, IITextContainer paragraphContainer) throws Exception {
        this.currentRunX = 0.0F;
        super.visitBR(br, paragraphContainer);
    }

    @Override
    protected void pageBreak() throws Exception {
        this.pdfDocument.pageBreak();
    }

    @Override
    protected void visitBookmark(CTBookmark bookmark, XWPFParagraph paragraph, IITextContainer paragraphContainer) throws Exception {
        Chunk chunk = new Chunk("\t");
        chunk.setLocalDestination(bookmark.getName());
        paragraphContainer.addElement(chunk);
    }

    @Override
    protected IITextContainer startVisitTable(XWPFTable table, float[] colWidths, IITextContainer pdfParentContainer) throws Exception {
        StylableTable pdfPTable = this.createPDFTable(table, colWidths, pdfParentContainer);
        return pdfPTable;
    }

    private StylableTable createPDFTable(XWPFTable table, float[] colWidths, IITextContainer pdfParentContainer) throws DocumentException {
        TableWidth tableWidth = this.stylesDocument.getTableWidth(table);
        StylableTable pdfPTable = this.pdfDocument.createTable(pdfParentContainer, colWidths.length);
        pdfPTable.setTotalWidth(colWidths);
        if (tableWidth != null && tableWidth.width > 0.0F) {
            if (tableWidth.percentUnit) {
                pdfPTable.setWidthPercentage(tableWidth.width);
            } else {
                pdfPTable.setTotalWidth(tableWidth.width);
            }
        }

        pdfPTable.setLockedWidth(true);
        ParagraphAlignment alignment = this.stylesDocument.getTableAlignment(table);
        if (alignment != null) {
            switch(alignment) {
            case LEFT:
                pdfPTable.setHorizontalAlignment(0);
                break;
            case RIGHT:
                pdfPTable.setHorizontalAlignment(2);
                break;
            case CENTER:
                pdfPTable.setHorizontalAlignment(1);
                break;
            case BOTH:
                pdfPTable.setHorizontalAlignment(3);
            }
        }

        Float indentation = this.stylesDocument.getTableIndentation(table);
        if (indentation != null) {
            pdfPTable.setPaddingLeft(indentation);
        }

        return pdfPTable;
    }

    @Override
    protected void endVisitTable(XWPFTable table, IITextContainer pdfParentContainer, IITextContainer pdfTableContainer) throws Exception {
        pdfParentContainer.addElement(((ExtendedPdfPTable)pdfTableContainer).getElement());
    }

    @Override
    protected void startVisitTableRow(XWPFTableRow row, IITextContainer tableContainer, int rowIndex, boolean headerRow) throws Exception {
        if (headerRow) {
            PdfPTable table = (PdfPTable)tableContainer;
            table.setHeaderRows(table.getHeaderRows() + 1);
        }

        super.startVisitTableRow(row, tableContainer, rowIndex, headerRow);
    }

    @Override
    protected IITextContainer startVisitTableCell(XWPFTableCell cell, IITextContainer pdfTableContainer, boolean firstRow, boolean lastRow, boolean firstCol, boolean lastCol, List<XWPFTableCell> vMergeCells) throws Exception {
        XWPFTableRow row = cell.getTableRow();
        XWPFTable table = row.getTable();
        this.stylesDocument.getTableInfo(table).addCellInfo(cell, firstRow, lastRow, firstCol, lastCol);
        StylableTable pdfPTable = (StylableTable)pdfTableContainer;
        StylableTableCell pdfPCell = this.pdfDocument.createTableCell(pdfPTable);
        XWPFTableCell lastVMergedCell = null;
        if (vMergeCells != null) {
            pdfPCell.setRowspan(vMergeCells.size());
            lastVMergedCell = (XWPFTableCell)vMergeCells.get(vMergeCells.size() - 1);
            this.stylesDocument.getTableInfo(table).addCellInfo(lastVMergedCell, false, lastRow, firstCol, lastCol);
        }

        TableCellBorder borderTop = this.stylesDocument.getTableCellBorderWithConflicts(cell, BorderSide.TOP);
        if (borderTop != null) {
            boolean borderTopInside = this.stylesDocument.isBorderInside(cell, BorderSide.TOP);
            if (borderTopInside) {
            }
        }

        pdfPCell.setBorderTop(borderTop, false);
        XWPFTableCell theCell = lastVMergedCell != null ? lastVMergedCell : cell;
        TableCellBorder borderBottom = this.stylesDocument.getTableCellBorderWithConflicts(theCell, BorderSide.BOTTOM);
        pdfPCell.setBorderBottom(borderBottom, this.stylesDocument.isBorderInside(theCell, BorderSide.BOTTOM));
        TableCellBorder borderLeft = this.stylesDocument.getTableCellBorderWithConflicts(cell, BorderSide.LEFT);
        pdfPCell.setBorderLeft(borderLeft, this.stylesDocument.isBorderInside(cell, BorderSide.LEFT));
        TableCellBorder borderRight = this.stylesDocument.getTableCellBorderWithConflicts(cell, BorderSide.RIGHT);
        pdfPCell.setBorderRight(borderRight, this.stylesDocument.isBorderInside(cell, BorderSide.RIGHT));
        CTTextDirection direction = this.stylesDocument.getTextDirection(cell);
        if (direction != null) {
            int dir = direction.getVal().intValue();
            switch(dir) {
            case 2:
                pdfPCell.setRotation(270);
                break;
            case 3:
                pdfPCell.setRotation(90);
            }
        }

        BigInteger gridSpan = this.stylesDocument.getTableCellGridSpan(cell);
        if (gridSpan != null) {
            pdfPCell.setColspan(gridSpan.intValue());
        }

        Color backgroundColor = this.stylesDocument.getTableCellBackgroundColor(cell);
        if (backgroundColor != null) {
            pdfPCell.setBackgroundColor(Converter.toAwtColor(backgroundColor));
        }

        STVerticalJc.Enum jc = this.stylesDocument.getTableCellVerticalAlignment(cell);
        if (jc != null) {
            switch(jc.intValue()) {
            case 1:
                pdfPCell.setVerticalAlignment(4);
                break;
            case 2:
                pdfPCell.setVerticalAlignment(5);
            case 3:
            default:
                break;
            case 4:
                pdfPCell.setVerticalAlignment(6);
            }
        }

        Float marginTop = this.stylesDocument.getTableCellMarginTop(cell);
        if (marginTop == null) {
            marginTop = this.stylesDocument.getTableRowMarginTop(row);
            if (marginTop == null) {
                marginTop = this.stylesDocument.getTableMarginTop(table);
            }
        }

        if (marginTop != null) {
            pdfPCell.setPaddingTop(marginTop);
        }

        Float marginBottom = this.stylesDocument.getTableCellMarginBottom(cell);
        if (marginBottom == null) {
            marginBottom = this.stylesDocument.getTableRowMarginBottom(row);
            if (marginBottom == null) {
                marginBottom = this.stylesDocument.getTableMarginBottom(table);
            }
        }

        if (marginBottom != null && marginBottom > 0.0F) {
            pdfPCell.setPaddingBottom(marginBottom);
        }

        Float marginLeft = this.stylesDocument.getTableCellMarginLeft(cell);
        if (marginLeft == null) {
            marginLeft = this.stylesDocument.getTableRowMarginLeft(row);
            if (marginLeft == null) {
                marginLeft = this.stylesDocument.getTableMarginLeft(table);
            }
        }

        if (marginLeft != null) {
            pdfPCell.setPaddingLeft(marginLeft);
        }

        Float marginRight = this.stylesDocument.getTableCellMarginRight(cell);
        if (marginRight == null) {
            marginRight = this.stylesDocument.getTableRowMarginRight(row);
            if (marginRight == null) {
                marginRight = this.stylesDocument.getTableMarginRight(table);
            }
        }

        if (marginRight != null) {
            pdfPCell.setPaddingRight(marginRight);
        }

        TableHeight tableHeight = this.stylesDocument.getTableRowHeight(row);
        if (tableHeight != null) {
            if (tableHeight.minimum) {
                pdfPCell.setMinimumHeight(tableHeight.height);
            } else {
                pdfPCell.setFixedHeight(tableHeight.height);
            }
        }

        Boolean noWrap = this.stylesDocument.getTableCellNoWrap(cell);
        if (noWrap != null) {
            pdfPCell.setNoWrap(noWrap);
        }
        return pdfPCell;
    }

    @Override
    protected void endVisitTableCell(XWPFTableCell cell, IITextContainer tableContainer, IITextContainer tableCellContainer) {
        ExtendedPdfPTable pdfPTable = (ExtendedPdfPTable)tableContainer;
        ExtendedPdfPCell pdfPCell = (ExtendedPdfPCell)tableCellContainer;
        pdfPTable.addCell(pdfPCell);
    }

    @Override
    protected void visitPicture(CTPicture picture, Float offsetX, STRelFromH.Enum relativeFromH, Float offsetY, STRelFromV.Enum relativeFromV, org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STWrapText.Enum wrapText, IITextContainer pdfParentContainer) throws Exception {
        CTPositiveSize2D ext = picture.getSpPr().getXfrm().getExt();
        long x = ext.getCx();
        long y = ext.getCy();
        XWPFPictureData pictureData = super.getPictureData(picture);
        if (pictureData != null) {
            try {
                Image img = Image.getInstance(pictureData.getData());
                img.scaleAbsolute(DxaUtil.emu2points(x), DxaUtil.emu2points(y));
                IITextContainer parentOfParentContainer = pdfParentContainer.getITextContainer();
                if (parentOfParentContainer != null && parentOfParentContainer instanceof PdfPCell) {
                    parentOfParentContainer.addElement(img);
                } else {
                    float chunkOffsetX = 0.0F;
                    if (offsetX != null) {
                        if (STRelFromH.CHARACTER.equals(relativeFromH)) {
                            chunkOffsetX = offsetX;
                        } else if (STRelFromH.COLUMN.equals(relativeFromH)) {
                            chunkOffsetX = offsetX;
                        } else if (STRelFromH.INSIDE_MARGIN.equals(relativeFromH)) {
                            chunkOffsetX = offsetX;
                        } else if (STRelFromH.LEFT_MARGIN.equals(relativeFromH)) {
                            chunkOffsetX = offsetX;
                        } else if (STRelFromH.MARGIN.equals(relativeFromH)) {
                            chunkOffsetX = this.pdfDocument.left() + offsetX;
                        } else if (STRelFromH.OUTSIDE_MARGIN.equals(relativeFromH)) {
                            chunkOffsetX = offsetX;
                        } else if (STRelFromH.PAGE.equals(relativeFromH)) {
                            chunkOffsetX = offsetX - this.pdfDocument.left();
                        }
                    }

                    float chunkOffsetY = 0.0F;
                    boolean useExtendedImage = false;
                    if (STRelFromV.PARAGRAPH.equals(relativeFromV)) {
                        useExtendedImage = true;
                    }

                    if (useExtendedImage) {
                        ExtendedImage extImg = new ExtendedImage(img, -offsetY);
                        if (STRelFromV.PARAGRAPH.equals(relativeFromV)) {
                            chunkOffsetY = -extImg.getScaledHeight();
                        }

                        Chunk chunk = new Chunk(extImg, chunkOffsetX, chunkOffsetY, false);
                        pdfParentContainer.addElement(chunk);
                    } else {
                        if (pdfParentContainer instanceof Paragraph) {
                            Paragraph paragraph = (Paragraph)pdfParentContainer;
                            paragraph.setSpacingBefore(paragraph.getSpacingBefore() + 5.0F);
                        }

                        pdfParentContainer.addElement(new Chunk(img, chunkOffsetX, chunkOffsetY, false));
                    }
                }
            } catch (Exception var21) {
                LOGGER.severe(var21.getMessage());
            }
        }

    }

    public int getPageCount() {
        return this.pdfDocument.isOpen() ? this.pdfDocument.getPageNumber() : this.pdfDocument.getPageNumber() - 1;
    }

    public boolean useTotalPageField() {
        return this.totalPageFieldUsed;
    }
}
