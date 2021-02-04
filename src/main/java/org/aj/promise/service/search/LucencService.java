package org.aj.promise.service.search;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.aj.promise.domain.Blog;
import org.aj.promise.domain.Comment;
import org.aj.promise.properties.CommonProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LucencService {

    private final Path rootLocation;

    @Autowired
    public LucencService(CommonProperties properties) {
        this.rootLocation = Paths.get(properties.getLucenceIndexes());
    }

    public void addBlogIndex(Blog blog) {
        writeIndex(Blog.INDEX, doc -> {
            doc.setId(blog.getId());
            doc.add(new TextField(Blog.ID, blog.getId(), Field.Store.YES));
            String content = blog.getTitle() + " " + blog.getContent();
            doc.add(new TextField(Blog.CONTENT, getTextFromHtml(content), Field.Store.NO));
        });
    }

    public void addCommentIndex(Comment comment) {
        writeIndex(Comment.INDEX, doc -> {
            doc.setId(comment.getId());
            doc.add(new TextField(Blog.ID, comment.getId(), Field.Store.NO));
            doc.add(new StringField(Comment.BLOG_ID, comment.getBlogId(), Field.Store.YES));
            doc.add(new TextField(Comment.CONTENT, getTextFromHtml(comment.getContent()), Field.Store.NO));
        });
    }

    private String getTextFromHtml(String html) {
        try {
            StringReader in = new StringReader(html);
            StringBuffer buffer = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            delegator.parse(in, new HTMLEditorKit.ParserCallback() {
                public void handleText(char[] text, int pos) {
                    buffer.append(text);
                }
            }, Boolean.TRUE);
            return buffer.toString().replace("\\n", "");
        } catch (Exception e) {
            log.error("getTextFromHtml error", e);
            return html;
        }
    }

    private void writeIndex(String index, Consumer<MyDocument> fun) {
        try {
            Directory dir = FSDirectory.open(rootLocation.resolve(index));
            Analyzer analyzer = new SmartChineseAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(dir, iwc);
            MyDocument doc = new MyDocument();
            fun.accept(doc);
            if (StringUtils.isNotBlank(doc.id)) {
                doc.add(new StringField(MyDocument.ID, doc.id, Field.Store.YES));
                writer.updateDocument(new Term(MyDocument.ID, doc.id), doc.doc);
            } else {
                writer.addDocument(doc.doc);
            }
            writer.close();
        } catch (Exception e) {
            log.error("writeIndex error", e);
        }
    }

    public static class MyDocument {
        public static String ID = "_id";
        public static String TIME = "_time";
        public Document doc;
        public String id;

        public MyDocument() {
            doc = new Document();
            doc.add(new LongPoint(TIME, System.currentTimeMillis()));
        }

        public void setId(String id) {
            this.id = id;
        }

        public void add(IndexableField field) {
            doc.add(field);
        }
    }

    private <T> List<T> searchIndex(String index, String field, String s, int n,
            Function<List<Document>, List<T>> fun) {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(rootLocation.resolve(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new SmartChineseAnalyzer();
            QueryParser parser = new QueryParser(field, analyzer);
            Query query = parser.parse(s);
            TopDocs results = searcher.search(query, n);
            ScoreDoc[] hits = results.scoreDocs;
            List<Document> docs = new ArrayList<>();
            for (ScoreDoc item : hits) {
                docs.add(searcher.doc(item.doc));
            }
            List<T> lst = fun.apply(docs);
            reader.close();
            return lst;
        } catch (Exception e) {
            log.error("searchIndex error", e);
            return Collections.emptyList();
        }
    }

    public List<String> searchBlogIds(String s) {
        ArrayList<String> lst = new ArrayList<>();
        List<String> ids = searchIndex(Blog.INDEX, Blog.CONTENT, s, 30, docs -> {
            List<String> blogIds = new ArrayList<>();
            for (Document doc : docs) {
                String id = doc.get(Blog.ID);
                if (id != null) {
                    blogIds.add(id);
                }
            }
            return blogIds;
        });
        lst.addAll(ids);
        List<String> cids = searchIndex(Comment.INDEX, Comment.CONTENT, s, 30, docs -> {
            List<String> cblogIds = new ArrayList<>();
            for (Document doc : docs) {
                String id = doc.get(Comment.BLOG_ID);
                if (id != null) {
                    cblogIds.add(id);
                }
            }
            return cblogIds;
        });

        for (int i = cids.size() - 1; i >= 0; i--) {
            String id = cids.get(i);
            if (lst.contains(id)) {
                lst.remove(id);
                lst.add(0, id);
            } else {
                lst.add(id);
            }
        }
        return lst;
    }
}
