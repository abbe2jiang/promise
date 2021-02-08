package org.aj.promise.service.search;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aj.promise.domain.Blog;
import org.aj.promise.domain.Comment;
import org.aj.promise.properties.CommonProperties;
import org.aj.promise.service.blog.BlogService;
import org.aj.promise.service.comment.CommentService;
import org.aj.promise.util.CommonUtil;
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

    public static String INDEX = "blog-comment";
    public static String ID = "id";
    public static String CONTENT = "content";

    private final Path rootLocation;

    @Autowired
    CommentService commentService;

    @Autowired
    BlogService blogService;

    @Autowired
    public LucencService(CommonProperties properties) {
        this.rootLocation = Paths.get(properties.getLucenceIndexes());
    }

    public void addBlogIndex(Blog blog) {
        doAddBlogIndex(blog);
    }

    private void doAddBlogIndex(Blog blog) {
        writeIndex(INDEX, doc -> {
            doc.setId(blog.getId());
            doc.add(new TextField(ID, blog.getId(), Field.Store.YES));
            String content = blog.getTitle() + " " + blog.getContent();
            List<Comment> comments = commentService.getComments(blog.getId());
            for (Comment comment : comments) {
                content += " " + comment.getContent();
            }
            doc.add(new TextField(CONTENT, CommonUtil.getTextFromHtml(content), Field.Store.NO));
        });
    }

    public void addCommentIndex(Comment comment) {
        Blog blog = blogService.getBlog(comment.getBlogId());
        doAddBlogIndex(blog);
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
        return searchIndex(INDEX, CONTENT, s, 30, docs -> {
            List<String> blogIds = new ArrayList<>();
            for (Document doc : docs) {
                String id = doc.get(ID);
                if (id != null) {
                    blogIds.add(id);
                }
            }
            return blogIds;
        });
    }
}
