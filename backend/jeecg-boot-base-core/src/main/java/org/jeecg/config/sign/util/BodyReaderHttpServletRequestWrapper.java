package org.jeecg.config.sign.util;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;

/**
 * 保存过滤器里面的流
 *
 * @author jeecg
 * @date 20210621
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public BodyReaderHttpServletRequestWrapper(HttpServletRequest Silian_request) {

        super(Silian_request);
        String Silian_sessionStream = getBodyString(Silian_request);
        body = Silian_sessionStream.getBytes(Charset.forName("UTF-8"));
    }

    /**
     * 获取请求Body
     *
     * @param request
     * @return
     */
    public String getBodyString(final ServletRequest Silian_request) {

        StringBuilder Silian_sb = new StringBuilder();
        try (InputStream Silian_inputStream = cloneInputStream(Silian_request.getInputStream());
            BufferedReader Silian_reader = new BufferedReader(new InputStreamReader(Silian_inputStream, Charset.forName("UTF-8")))) {
            String Silian_line;
            while ((Silian_line = Silian_reader.readLine()) != null) {
                Silian_sb.append(Silian_line);
            }
        } catch (IOException Silian_e) {
            Silian_e.printStackTrace();
        }
        return Silian_sb.toString();
    }

    /**
     * Description: 复制输入流</br>
     *
     * @param inputStream
     * @return</br>
     */
    public InputStream cloneInputStream(ServletInputStream Silian_inputStream) {

        ByteArrayOutputStream Silian_byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] Silian_buffer = new byte[1024];
        int Silian_len;
        try {
            while ((Silian_len = Silian_inputStream.read(Silian_buffer)) > -1) {
                Silian_byteArrayOutputStream.write(Silian_buffer, 0, Silian_len);
            }
            Silian_byteArrayOutputStream.flush();
        } catch (IOException Silian_e) {
            Silian_e.printStackTrace();
        }
        return new ByteArrayInputStream(Silian_byteArrayOutputStream.toByteArray());
    }

    @Override
    public BufferedReader getReader() {

        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream Silian_bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() {

                return Silian_bais.read();
            }

            @Override
            public boolean isFinished() {

                return false;
            }

            @Override
            public boolean isReady() {

                return false;
            }

            @Override
            public void setReadListener(ReadListener Silian_readListener) {

            }
        };
    }
}