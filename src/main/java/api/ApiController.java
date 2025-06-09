package api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import database.Database1;
import tracker.Tracker;
import cheat.Cheat;

import entity.Seed;
import entity.User;
import entity.Post;
import entity.BegSeedDetail;
import entity.PostReply;
import entity.Profile;
import entity.UserPT;
import entity.config;
import entity.Appeal;
import entity.BegInfo;
import entity.BegInfoDetail;
import entity.UserStar;
import entity.SeedWithVotes;
import entity.SeedPromotion;
import entity.SeedWithPromotionDTO;

import java.util.UUID;

@RestController
public class ApiController implements ApiInterface {

    private static Database1 db1;
    private static Tracker tracker;
    private static Cheat cheat;
    private static ObjectMapper mapper;
    private static HttpHeaders headers;
    private static HttpHeaders errorHeaders;

    @PostConstruct
    public void init() {
        cheat = new Cheat();
        db1 = new Database1();
        tracker = new Tracker();
        mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        errorHeaders = new HttpHeaders();
        errorHeaders.add("Access-Control-Allow-Origin", "*");
        errorHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        errorHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    public ResponseEntity<Integer> saveTorrent(
        @RequestParam("userid") String userid,
        @RequestParam("title") String title,
        @RequestParam("tag") String tag,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            Seed seed = new Seed();
            seed.seedid = UUID.randomUUID().toString(); // 生成唯一的种子ID
            seed.seeduserid = userid;
            seed.title = title;
            seed.seedsize = "1GB";
            seed.seedtag = tag;
            seed.url = "http://example.com/torrent"; // 示例URL
            int ret = db1.RegisterSeed(seed);
            if (ret != 0) {
                // 如果注册种子失败，返回错误状态
                return new ResponseEntity<>(ret, headers, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            File tempFile = File.createTempFile(userid, file.getOriginalFilename());
            file.transferTo(tempFile);
            tracker.SaveTorrent(seed.seedid, tempFile);
            return new ResponseEntity<>(0, headers, HttpStatus.OK); // 返回 0 表示成功
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示失败
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*") // 允许所有来源和头部
    public ResponseEntity<Resource> getTorrent(
        @RequestParam("torrentId") String seedid,
        @RequestParam("userId") String userid
    ) {
        File file = tracker.GetTTorent(seedid, userid);
        if (file != null) {
            FileSystemResource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION) // 关键：允许前端访问Content-Disposition
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<String> getSeedListByTag(
        @RequestParam("tag") String tag
    ) {
        try {
            SeedWithPromotionDTO[] seeds = db1.GetSeedListByTag(tag);
            if (seeds == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 404 表示未找到种子
            }
            String json = mapper.writeValueAsString(seeds);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getUserProfile(
        @RequestParam("userid") String userid
    ) {
        try {
            User user = db1.GetInformation(userid);
            if (user == null) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String json = mapper.writeValueAsString(user);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> changeProfile(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            
            // 安全地获取 userid 字段
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            if (useridNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            String userid = useridNode.asText();
            
            // 添加参数验证
            if (userid == null || userid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            // 手动映射前端字段到 User 对象，处理类型转换
            User user = new User();
            user.userid = userid;
            
            // 安全地获取其他字段并进行类型转换
            if (jsonNode.has("username") && !jsonNode.get("username").isNull()) {
                user.username = jsonNode.get("username").asText();
            }
            if (jsonNode.has("school") && !jsonNode.get("school").isNull()) {
                user.school = jsonNode.get("school").asText();
            }
            if (jsonNode.has("gender") && !jsonNode.get("gender").isNull()) {
                user.sex = jsonNode.get("gender").asText();
            }
            if (jsonNode.has("avatar_url") && !jsonNode.get("avatar_url").isNull()) {
                user.pictureurl = jsonNode.get("avatar_url").asText();
            }
            
            // 处理 account_status 的类型转换（字符串/数字 -> 布尔值）
            if (jsonNode.has("account_status") && !jsonNode.get("account_status").isNull()) {
                com.fasterxml.jackson.databind.JsonNode statusNode = jsonNode.get("account_status");
                if (statusNode.isTextual()) {
                    String statusStr = statusNode.asText();
                    user.accountstate = "1".equals(statusStr) || "封禁".equals(statusStr);
                } else if (statusNode.isNumber()) {
                    user.accountstate = statusNode.asInt() == 1;
                } else if (statusNode.isBoolean()) {
                    user.accountstate = statusNode.asBoolean();
                }
            }
            
            // 处理 invite_left 的类型转换（字符串 -> 整数）
            if (jsonNode.has("invite_left") && !jsonNode.get("invite_left").isNull()) {
                com.fasterxml.jackson.databind.JsonNode inviteNode = jsonNode.get("invite_left");
                if (inviteNode.isTextual()) {
                    try {
                        user.invitetimes = Integer.parseInt(inviteNode.asText());
                    } catch (NumberFormatException e) {
                        user.invitetimes = 0; // 默认值
                    }
                } else if (inviteNode.isNumber()) {
                    user.invitetimes = inviteNode.asInt();
                }
            }
            
            int ret = db1.UpdateInformation(user);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(ret, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getUserSeeds(
        @RequestParam("userid") String userid
    ) {
        try {
            Seed[] seeds = db1.GetSeedListByUser(userid);
            if (seeds == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 404 表示未找到种子
            }
            String json = mapper.writeValueAsString(seeds);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> deleteSeed(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode seedidNode = jsonNode.get("seedid");
            if (seedidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            String seedid = seedidNode.asText();
            
            // 添加参数验证
            if (seedid == null || seedid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            int ret = db1.DeleteSeed(seedid);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(ret, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getUserStat(
        @RequestParam("userid") String userid
    ) {
        try {
            User user = db1.GetInformation(userid);
            if (user == null) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String json = mapper.writeValueAsString(user);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getTorrentDetail(
        @RequestParam("id") String seedid
    ) {
        try {
            Seed seed = db1.GetSeedInformation(seedid);
            if (seed != null) {
                String json = mapper.writeValueAsString(seed);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                return new ResponseEntity<>(json, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build(); // 返回 404 表示种子未找到
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            HttpHeaders errorHeaders = new HttpHeaders();
            errorHeaders.add("Access-Control-Allow-Origin", "*");
            errorHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            errorHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            return new ResponseEntity<>("{}", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<String> loginUser(
        @RequestBody String requestBody
    ) {
        try {
            // 解析前端发送的JSON数据 {email: xxx, password: xxx}
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            
            // 获取email和password字段
            com.fasterxml.jackson.databind.JsonNode emailNode = jsonNode.get("email");
            com.fasterxml.jackson.databind.JsonNode passwordNode = jsonNode.get("password");

            if (emailNode == null || passwordNode == null) {
                com.fasterxml.jackson.databind.node.ObjectNode errorJson = mapper.createObjectNode();
                errorJson.put("message", "缺少必要参数");
                String jsonError = mapper.writeValueAsString(errorJson);
                return new ResponseEntity<>(jsonError, HttpStatus.BAD_REQUEST);
            }
            
            String email = emailNode.asText();
            String password = passwordNode.asText();

            // 参数验证
            if (email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                com.fasterxml.jackson.databind.node.ObjectNode errorJson = mapper.createObjectNode();
                errorJson.put("message", "邮箱和密码不能为空");
                String jsonError = mapper.writeValueAsString(errorJson);
                return new ResponseEntity<>(jsonError, HttpStatus.BAD_REQUEST);
            }
            
            // 创建User对象进行登录验证
            User user = new User();
            user.email = email.trim();
            user.password = password;
            
            String userid = db1.LoginUser(user);
            if (userid != null) {
                com.fasterxml.jackson.databind.node.ObjectNode responseJson = mapper.createObjectNode();
                responseJson.put("userId", userid);
                responseJson.put("userid", userid);
                responseJson.put("message", "登录成功");
                String jsonResponse = mapper.writeValueAsString(responseJson);
                return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            } else {
                // 返回JSON格式的错误信息
                com.fasterxml.jackson.databind.node.ObjectNode errorJson = mapper.createObjectNode();
                errorJson.put("message", "登录失败，请检查账号密码");
                String jsonError = mapper.writeValueAsString(errorJson);
                return new ResponseEntity<>(jsonError, HttpStatus.UNAUTHORIZED);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            com.fasterxml.jackson.databind.node.ObjectNode errorJson = mapper.createObjectNode();
            try {
                errorJson.put("message", "服务器内部错误");
                String jsonError = mapper.writeValueAsString(errorJson);
                return new ResponseEntity<>(jsonError, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonProcessingException ex) {
                return new ResponseEntity<>("{\"message\":\"服务器内部错误\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            com.fasterxml.jackson.databind.node.ObjectNode errorJson = mapper.createObjectNode();
            try {
                errorJson.put("message", "服务器内部错误");
                String jsonError = mapper.writeValueAsString(errorJson);
                return new ResponseEntity<>(jsonError, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonProcessingException ex) {
                return new ResponseEntity<>("{\"message\":\"服务器内部错误\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> registerUser(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            
            // 安全地获取字段
            com.fasterxml.jackson.databind.JsonNode usernameNode = jsonNode.get("username");
            com.fasterxml.jackson.databind.JsonNode passwordNode = jsonNode.get("password");
            com.fasterxml.jackson.databind.JsonNode inviteEmailNode = jsonNode.get("invite_email");
            
            if (usernameNode == null || passwordNode == null || inviteEmailNode == null) {
                return new ResponseEntity<>(2, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            
            String username = usernameNode.asText();
            String password = passwordNode.asText();
            String inviteEmail = inviteEmailNode.asText();
            
            // 参数验证
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty() ||
                inviteEmail == null || inviteEmail.trim().isEmpty()) {
                return new ResponseEntity<>(2, HttpStatus.BAD_REQUEST);
            }
            
            // 创建 User 对象
            User user = new User();
            user.userid = java.util.UUID.randomUUID().toString(); // 生成唯一用户ID
            user.username = username.trim();
            user.password = password;
            user.email = inviteEmail.trim(); // 使用邀请邮箱作为用户邮箱
            
            // 设置默认值
            user.sex = "m"; // 默认性别
            user.school = ""; // 默认学校
            user.pictureurl = ""; // 默认头像URL
            user.profile = ""; // 默认个人简介
            user.accountstate = false; // 默认账号状态为正常
            user.invitetimes = 5; // 默认邀请次数
            
            // 设置时间字段
            user.lastDetectedTime = new java.util.Date();
            user.fakeLastDetectedTime = new java.util.Date();
            
            // 调用数据库注册方法
            int ret = db1.RegisterUser(user);
            
            // // 如果注册成功，还需要创建对应的 UserPT 记录
            // if (ret == 0) {
            //     try {
            //         entity.UserPT userPT = new entity.UserPT();
            //         userPT.userid = user.userid;
            //         userPT.magic = 100; // 初始魔力值
            //         userPT.upload = 0L; // 初始上传量
            //         userPT.download = 0L; // 初始下载量
            //         userPT.share = 0.0; // 初始分享率
            //         userPT.farmurl = ""; // 默认做种路径
            //         userPT.viptime = 0; // 初始VIP次数
            //         userPT.user = user; // 设置关联
                    
            //         int ptRet = db1.RegisterUserPT(userPT);
            //         if (ptRet != 0) {
            //             // 如果 UserPT 创建失败，记录日志但不影响主要注册流程
            //             System.err.println("Warning: Failed to create UserPT for user " + user.userid);
            //         }
            //     } catch (Exception e) {
            //         System.err.println("Warning: Exception creating UserPT: " + e.getMessage());
            //     }
            // }
            
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示注册成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.CONFLICT); // 返回 1 表示邮箱重复
            } else {
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST); // 返回 2 表示未被邀请或其他错误
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 2 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<String> getForum() {
        try {
            Post[] posts = db1.GetPostList();
            if (posts == null) {
                return new ResponseEntity<>("[]", HttpStatus.OK); // 返回空数组表示没有帖子
            }
            String json = mapper.writeValueAsString(posts);
            return new ResponseEntity<>(json, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<String> getPostById(
        @RequestParam("postid") String postid
    ) {
        try {
            Post post = db1.GetPost(postid);
            PostReply[] replies = db1.GetPostReplyList(postid);
            if (post == null) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND); // 返回 404 表示帖子未找到
            }
            
            // 创建合并的 JSON 对象
            com.fasterxml.jackson.databind.node.ObjectNode resultJson = mapper.createObjectNode();
            
            // 添加 post 的所有字段
            resultJson.put("postid", post.postid);
            resultJson.put("posttitle", post.posttitle);
            resultJson.put("postcontent", post.postcontent);
            resultJson.put("postuserid", post.postuserid);
            resultJson.put("replytime", post.replytime);
            resultJson.put("readtime", post.readtime);
            
            if (post.posttime != null) {
                resultJson.put("posttime", post.posttime.getTime());
            }
            
            // 添加作者信息
            if (post.author != null) {
                com.fasterxml.jackson.databind.node.ObjectNode authorJson = mapper.createObjectNode();
                authorJson.put("userid", post.author.userid);
                authorJson.put("username", post.author.username);
                authorJson.put("email", post.author.email);
                authorJson.put("sex", post.author.sex);
                authorJson.put("school", post.author.school);
                authorJson.put("pictureurl", post.author.pictureurl);
                authorJson.put("profile", post.author.profile);
                authorJson.put("accountstate", post.author.accountstate);
                authorJson.put("invitetimes", post.author.invitetimes);
                if (post.author.lastDetectedTime != null) {
                    authorJson.put("lastDetectedTime", post.author.lastDetectedTime.getTime());
                }
                if (post.author.fakeLastDetectedTime != null) {
                    authorJson.put("fakeLastDetectedTime", post.author.fakeLastDetectedTime.getTime());
                }
                resultJson.set("author", authorJson);
            }
            
            // 添加 replies 数组
            com.fasterxml.jackson.databind.node.ArrayNode repliesArray = mapper.createArrayNode();
            if (replies != null) {
                for (PostReply reply : replies) {
                    com.fasterxml.jackson.databind.node.ObjectNode replyJson = mapper.createObjectNode();
                    replyJson.put("replyid", reply.replyid);
                    replyJson.put("postid", reply.postid);
                    replyJson.put("authorid", reply.authorid);
                    replyJson.put("content", reply.content);
                    if (reply.createdAt != null) {
                        replyJson.put("createdAt", reply.createdAt.getTime());
                    }
                    
                    // 添加回复作者信息
                    if (reply.author != null) {
                        com.fasterxml.jackson.databind.node.ObjectNode replyAuthorJson = mapper.createObjectNode();
                        replyAuthorJson.put("userid", reply.author.userid);
                        replyAuthorJson.put("username", reply.author.username);
                        replyAuthorJson.put("email", reply.author.email);
                        replyAuthorJson.put("sex", reply.author.sex);
                        replyAuthorJson.put("school", reply.author.school);
                        replyAuthorJson.put("pictureurl", reply.author.pictureurl);
                        replyAuthorJson.put("profile", reply.author.profile);
                        replyAuthorJson.put("accountstate", reply.author.accountstate);
                        replyAuthorJson.put("invitetimes", reply.author.invitetimes);
                        if (reply.author.lastDetectedTime != null) {
                            replyAuthorJson.put("lastDetectedTime", reply.author.lastDetectedTime.getTime());
                        }
                        if (reply.author.fakeLastDetectedTime != null) {
                            replyAuthorJson.put("fakeLastDetectedTime", reply.author.fakeLastDetectedTime.getTime());
                        }
                        replyJson.set("author", replyAuthorJson);
                    }
                    
                    repliesArray.add(replyJson);
                }
            }
            resultJson.set("replies", repliesArray);
            
            String json = mapper.writeValueAsString(resultJson);
            return new ResponseEntity<>(json, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            // e.printStackTrace();
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // e.printStackTrace();
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> addPostReply(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode postidNode = jsonNode.get("postid");
            com.fasterxml.jackson.databind.JsonNode authoridNode = jsonNode.get("replyuserid");
            com.fasterxml.jackson.databind.JsonNode contentNode = jsonNode.get("replycontent");

            if (postidNode == null || authoridNode == null || contentNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String postid = postidNode.asText();
            String authorid = authoridNode.asText();
            String content = contentNode.asText();

            // 参数验证
            if (postid == null || postid.trim().isEmpty() || 
                authorid == null || authorid.trim().isEmpty() || 
                content == null || content.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            int ret = db1.AddComment(postid, authorid, content);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(ret, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> searchSeeds(
        @RequestParam("tag") String tag,
        @RequestParam("keyword") String query
    ) {
        try {
            Seed[] seeds = db1.SearchSeed(query);
            if (seeds == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到种子
            }
            
            // 过滤掉与前端要求tag不同的种子
            java.util.List<Seed> filteredSeeds = new java.util.ArrayList<>();
            for (Seed seed : seeds) {
                if (seed.seedtag != null && seed.seedtag.equals(tag)) {
                    filteredSeeds.add(seed);
                }
            }
            
            // 如果过滤后没有匹配的种子，返回空数组
            if (filteredSeeds.isEmpty()) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND);
            }
            
            Seed[] filteredSeedsArray = filteredSeeds.toArray(new Seed[0]);
            String json = mapper.writeValueAsString(filteredSeedsArray);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> searchPosts(
        @RequestParam("keyword") String query
    ) {
        try {
            Post[] posts = db1.SearchPost(query);
            if (posts == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到帖子
            }
            String json = mapper.writeValueAsString(posts);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getUserPT(
        @RequestParam("userid") String userid
    ) {
        try {
            UserPT userPT = db1.GetInformationPT(userid);
            if (userPT == null) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到用户PT信息
            }
            String json = mapper.writeValueAsString(userPT);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getConfig(
        @RequestParam("userid") String userid
    ) {
        try {
            int ret = db1.CheckAdmin(userid);
            if (ret != 0) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.UNAUTHORIZED); // 返回 401 表示未授权
            }
            // 创建配置信息的 JSON 对象
            Map<String, Object> configData = new HashMap<>();
            configData.put("FarmNumber", config.getFarmNumber());
            configData.put("FakeTime", config.getFakeTime());
            configData.put("BegVote", config.getBegVote());
            configData.put("CheatTime", config.getCheatTime());
            
            String json = mapper.writeValueAsString(configData);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getCheatUsers(
        @RequestParam("userid") String userid
    ) {
        try {
            int ret = db1.CheckAdmin(userid);
            if (ret != 0) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.UNAUTHORIZED); // 返回 401 表示未授权
            }
            User[] cheatUsers = cheat.GetCheatUsers();
            if (cheatUsers == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到作弊用户
            }
            String json = mapper.writeValueAsString(cheatUsers);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getSuspiciousUsers(
        @RequestParam("userid") String userid
    ) {
        try {
            int ret = db1.CheckAdmin(userid);
            if (ret != 0) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.UNAUTHORIZED); // 返回 401 表示未授权
            }
            User[] suspiciousUsers = cheat.GetSuspiciousUsers();
            if (suspiciousUsers == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到可疑用户
            }
            String json = mapper.writeValueAsString(suspiciousUsers);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> unbanUser(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            if (useridNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String userid = useridNode.asText();
            
            // 添加参数验证
            if (userid == null || userid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            int ret = cheat.UnbanUser(userid);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.NOT_FOUND); // 返回 1 表示用户不存在
            } else if (ret == 2) {
                return new ResponseEntity<>(2, HttpStatus.CONFLICT); // 返回 2 表示用户未被封禁
            } else {
                return new ResponseEntity<>(3, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> banUser(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            if (useridNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String userid = useridNode.asText();
            
            // 添加参数验证
            if (userid == null || userid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            int ret = cheat.BanUser(userid);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.NOT_FOUND); // 返回 1 表示用户不存在
            } else if (ret == 2) {
                return new ResponseEntity<>(2, HttpStatus.CONFLICT); // 返回 2 表示用户已被封禁
            } else {
                return new ResponseEntity<>(3, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getAppeals() {
        try {
            Appeal[] appeals = cheat.GetAppealList();
            if (appeals == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到申诉
            }
            String json = mapper.writeValueAsString(appeals);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getMigrations() {
        try {
            Profile[] migrations = db1.GetTransmitProfileList();
            if (migrations == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到迁移记录
            }
            String json = mapper.writeValueAsString(migrations);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> approveAppeal(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode appealidNode = jsonNode.get("appealid");
            if (appealidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String appealid = appealidNode.asText();
            
            // 添加参数验证
            if (appealid == null || appealid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            boolean ret = cheat.HandleAppeal(appealid, 1);
            if (ret) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> rejectAppeal(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode appealidNode = jsonNode.get("appealid");
            if (appealidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String appealid = appealidNode.asText();
            
            // 添加参数验证
            if (appealid == null || appealid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            boolean ret = cheat.HandleAppeal(appealid, 2);
            if (ret) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> approveMigration(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode migrationidNode = jsonNode.get("migration_id");
            com.fasterxml.jackson.databind.JsonNode grantedUploadedNode = jsonNode.get("granted_uploaded");
            if (migrationidNode == null || grantedUploadedNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String migrationid = migrationidNode.asText();
            Integer grantedUploaded = grantedUploadedNode != null ? grantedUploadedNode.asInt() : null;
            if (grantedUploaded == null || grantedUploaded < 0) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 上传量必须为非负整数
            }
            
            // 添加参数验证
            if (migrationid == null || migrationid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            boolean ret = db1.ExamTransmitProfile(migrationid, true, grantedUploaded);
            if (ret) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> rejectMigration(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode migrationidNode = jsonNode.get("migration_id");
            if (migrationidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String migrationid = migrationidNode.asText();
            
            // 添加参数验证
            if (migrationid == null || migrationid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            boolean ret = db1.ExamTransmitProfile(migrationid, false, 0);
            if (ret) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> inviteUser(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode emailNode = jsonNode.get("invite_email");
            if (useridNode == null || emailNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String userid = useridNode.asText();
            String email = emailNode.asText();
            
            // 添加参数验证
            if (email == null || email.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            int ret = db1.InviteNewUser(userid, email);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.CONFLICT); // 返回 1 表示邀请已存在
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> submitAppeal(
        @RequestParam("userid") String userid,
        @RequestParam("content") String content,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            // 先进行参数验证
            if (userid == null || userid.trim().isEmpty() || 
                content == null || content.trim().isEmpty() ||
                file == null || file.isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            if (file.getSize() > 100 * 1024 * 1024) {
                return new ResponseEntity<>(3, HttpStatus.PAYLOAD_TOO_LARGE); // 返回 3 表示文件过大
            }
            
            File tempFile = File.createTempFile(userid, file.getOriginalFilename());
            file.transferTo(tempFile);
            
            int ret = cheat.SubmitAppeal(userid, content, tempFile);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.CONFLICT); // 返回 1 表示用户未被封禁或其他错误
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (org.springframework.web.multipart.MaxUploadSizeExceededException e) {
            return new ResponseEntity<>(3, HttpStatus.PAYLOAD_TOO_LARGE); // 返回 3 表示文件过大
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getUserStats(
        @RequestParam("userid") String userid
    ) {
        try {
            if (userid == null || userid.trim().isEmpty()) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.BAD_REQUEST); // 返回 400 表示参数不完整
            }

            UserPT userPT = db1.GetInformationPT(userid);
            if (userPT == null) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到用户PT信息
            }   

            String json = mapper.writeValueAsString(userPT);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> magicExchange(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode magicNode = jsonNode.get("magic");
            com.fasterxml.jackson.databind.JsonNode typeNode = jsonNode.get("exchangeType");

            if (useridNode == null || magicNode == null || typeNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }
            String userid = useridNode.asText();
            int magic = magicNode.asInt();
            String exchangeType = typeNode.asText();

            boolean ret = false;
            if( "uploaded".equals(exchangeType) ) {
                ret = db1.ExchangeMagicToUpload(userid, magic);
            } else if( "downloaded".equals(exchangeType) ) {
                ret = db1.ExchangeMagicToDownload(userid, magic);
            } else if( "vip_downloads".equals(exchangeType) ) {
                ret = db1.ExchangeMagicToVip(userid, magic);
            } else {
                return new ResponseEntity<>(2, HttpStatus.BAD_REQUEST); // 返回 2 表示交换类型错误
            }
            if (!ret) {
                return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
            }
            return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getUserFavorites(
        @RequestParam("userid") String userid
    ) {
        try {
            if (userid == null || userid.trim().isEmpty()) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.BAD_REQUEST); // 返回 400 表示参数不完整
            }

            UserStar[] favorites = db1.GetUserStarList(userid);
            if (favorites == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到用户收藏
            }

            String json = mapper.writeValueAsString(favorites);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> removeFavorite(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode seedidNode = jsonNode.get("seedid");
            if (useridNode == null || seedidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String userid = useridNode.asText();
            String seedid = seedidNode.asText();

            // 添加参数验证
            if (userid == null || userid.trim().isEmpty() || 
                seedid == null || seedid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }

            boolean ret = db1.DeleteCollect(userid, seedid);
            if (ret) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> addFavorite(
        @RequestBody String requestBody
    ) {
        try {
            // 解析 JSON 数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode seedidNode = jsonNode.get("seedid");
            if (useridNode == null || seedidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String userid = useridNode.asText();
            String seedid = seedidNode.asText();

            // 添加参数验证
            if (userid == null || userid.trim().isEmpty() || 
                seedid == null || seedid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }

            boolean ret = db1.AddCollect(userid, seedid);
            if (ret) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> migrateAccount(
        @RequestParam("userid") String userid,
        @RequestParam("file") MultipartFile file,
        @RequestParam("uploadtogive") String uploadtogive
    ) {
        try {
            // 先进行参数验证
            if (userid == null || userid.trim().isEmpty() || 
                file == null || file.isEmpty() ||
                uploadtogive == null || uploadtogive.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST);
            }
            
            if (file.getSize() > 100 * 1024 * 1024) {
                return new ResponseEntity<>(3, HttpStatus.PAYLOAD_TOO_LARGE); // 返回 3 表示文件过大
            }
            
            File tempFile = File.createTempFile(userid, file.getOriginalFilename());
            file.transferTo(tempFile);

            int ret = db1.UploadMigration(userid, tempFile, uploadtogive);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.CONFLICT); // 返回 1 表示用户已存在或其他错误
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getBegSeedList() {
        try {
            BegSeedDetail[] begSeedList = db1.GetBegList();
            if (begSeedList == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到种子列表
            }
            String json = mapper.writeValueAsString(begSeedList);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        }
    }

    @Override
    public ResponseEntity<String> getBegSeedDetail(
        @RequestParam("begid") String begid
    ) {
        try {
            if (begid == null || begid.trim().isEmpty()) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.BAD_REQUEST); // 返回 400 表示参数不完整
            }

            BegSeedDetail begSeedDetail = db1.GetBegSeedDetail(begid);
            if (begSeedDetail == null) {
                return new ResponseEntity<>("", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到种子详情
            }
            String json = mapper.writeValueAsString(begSeedDetail);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getBegSeedSubmissions(
        @RequestParam("begid") String begid
    ) {
        try {
            if (begid == null || begid.trim().isEmpty()) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.BAD_REQUEST); // 返回 400 表示参数不完整
            }

            SeedWithVotes[] submissionsWithVotes = db1.GetBegSeedListWithVotes(begid);
            if (submissionsWithVotes == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到提交记录
            }

            String json = mapper.writeValueAsString(submissionsWithVotes);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> submitSeed(
        @RequestBody String requestBody
    ) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode begidNode = jsonNode.get("begid");
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode seedidNode = jsonNode.get("seedid");
            
            if (begidNode == null || useridNode == null || seedidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String begid = begidNode.asText();
            String userid = useridNode.asText();
            String seedid = seedidNode.asText();

            if (begid == null || begid.trim().isEmpty() || 
                userid == null || userid.trim().isEmpty() || 
                seedid == null || seedid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            int ret = db1.SubmitBegSeed(begid, seedid, userid);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(1, HttpStatus.CONFLICT); // 返回 1 表示提交失败或其他错误
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> voteSeed(
        @RequestBody String requestBody
    ) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode begidNode = jsonNode.get("begid");
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode seedidNode = jsonNode.get("seedid");

            if (begidNode == null || useridNode == null || seedidNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String begid = begidNode.asText();
            String userid = useridNode.asText();
            String seedid = seedidNode.asText();

            if (begid == null || begid.trim().isEmpty() || 
                userid == null || userid.trim().isEmpty() || 
                seedid == null || seedid.trim().isEmpty()) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            int ret = db1.VoteSeed(begid, seedid, userid);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(3, HttpStatus.CONFLICT); // 返回 1 表示投票失败或其他错误
            } else {
                return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> createBegSeed(
        @RequestBody String requestBody
    ) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode useridNode = jsonNode.get("userid");
            com.fasterxml.jackson.databind.JsonNode infoNode = jsonNode.get("info");
            com.fasterxml.jackson.databind.JsonNode rewardNode = jsonNode.get("reward_magic");
            com.fasterxml.jackson.databind.JsonNode dedlineNode = jsonNode.get("deadline");

            if (useridNode == null || infoNode == null || rewardNode == null || dedlineNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String userid = useridNode.asText();
            String info = infoNode.asText();

            if (userid == null || userid.trim().isEmpty() ||
                info == null || info.trim().isEmpty() ||
                rewardNode.asInt() <= 0 ){
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            // 解析日期字符串
            Date endTime;
            try {
                String deadlineStr = dedlineNode.asText();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                endTime = dateFormat.parse(deadlineStr);
            } catch (ParseException e) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 日期格式错误
            }

            BegInfo beg = new BegInfo();
            beg.begid = java.util.UUID.randomUUID().toString();
            beg.begnumbers = 0; // 初始提交数为0
            beg.magic = rewardNode.asInt();
            beg.endtime = endTime;
            beg.hasseed = 0; // 初始状态为未开始

            // int ret = db1.AddBegSeed(beg);
            int ret = db1.createBagSeed(beg, userid, info);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(2, HttpStatus.CONFLICT); // 返回 1 表示创建失败或其他错误
            } else {
                return new ResponseEntity<>(3, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        }
    }

    @Override
    public ResponseEntity<String> getAllSeeds(){
        try {
            Seed[] allSeeds = db1.getAllSeeds();
            if (allSeeds == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到种子列表
            }
            String json = mapper.writeValueAsString(allSeeds);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getAllSeedPromotions() {
        try {
            SeedPromotion[] promotions = db1.getAllSeedPromotions();
            if (promotions == null) {
                return new ResponseEntity<>("[]", errorHeaders, HttpStatus.NOT_FOUND); // 返回 404 表示未找到种子推广
            }
            String json = mapper.writeValueAsString(promotions);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("[]", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Integer> createSeedPromotion(
        @RequestBody String requestBody
    ) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            com.fasterxml.jackson.databind.JsonNode seedidNode = jsonNode.get("seed_id");
            com.fasterxml.jackson.databind.JsonNode startTimeNode = jsonNode.get("start_time");
            com.fasterxml.jackson.databind.JsonNode endTimeNode = jsonNode.get("end_time");
            com.fasterxml.jackson.databind.JsonNode discountNode = jsonNode.get("discount");

            if (seedidNode == null || startTimeNode == null || endTimeNode == null || discountNode == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            String seedid = seedidNode.asText();
            Date startTime;
            Date endTime;
            Integer discount = discountNode.asInt();
            if (discount == null || discount < 0) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 折扣参数错误
            }
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                String startTimeStr = startTimeNode.asText();
                startTime = dateFormat.parse(startTimeStr);
                String endTimeStr = endTimeNode.asText();
                endTime = dateFormat.parse(endTimeStr);
            } catch (ParseException e) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 日期格式错误
            }

            if (seedid == null || seedid.trim().isEmpty() ||
                startTime == null || endTime == null) {
                return new ResponseEntity<>(1, HttpStatus.BAD_REQUEST); // 参数不完整
            }

            int ret = db1.createSeedPromotion(seedid, startTime, endTime, discount);
            if (ret == 0) {
                return new ResponseEntity<>(0, HttpStatus.OK); // 返回 0 表示成功
            } else if (ret == 1) {
                return new ResponseEntity<>(2, HttpStatus.CONFLICT); // 返回 1 表示创建失败或其他错误
            } else {
                return new ResponseEntity<>(3, HttpStatus.INTERNAL_SERVER_ERROR); // 返回其他状态表示失败
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 1 表示处理失败
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}