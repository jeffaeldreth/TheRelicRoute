package webView;

import java.io.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A CookieStore that saves cookies to a file on disk and reloads them on
 * startup, so sites like DuckDuckGo remember their settings (theme, safe
 * search, region, etc.) between app restarts — same as any normal browser.
 *
 * This wraps an in-memory list for actual lookups during runtime, and only
 * touches the file on load (startup) and save (call save() before closing).
 */
public class PersistentCookieStore implements CookieStore {

    private final List<HttpCookie> cookies = new ArrayList<>();
    private final File file;

    public PersistentCookieStore(File file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<SerializableCookie> saved = (List<SerializableCookie>) in.readObject();

            for (SerializableCookie sc : saved) {
                HttpCookie cookie = sc.toHttpCookie();
                if (!cookie.hasExpired()) {
                    cookies.add(cookie);
                }
            }
        } catch (Exception e) {
            // Corrupt or missing cookie file — just start fresh rather than crash
            //System.out.println("Could not load saved cookies, starting fresh: " + e.getMessage());
        }
    }

    public void save() {
        List<SerializableCookie> toSave = new ArrayList<>();
        for (HttpCookie cookie : cookies) {
            if (!cookie.hasExpired()) {
                toSave.add(new SerializableCookie(cookie));
            }
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(toSave);
        } catch (IOException e) {
            //System.out.println("Could not save cookies: " + e.getMessage());
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        synchronized (cookies) {
            cookies.removeIf(c -> c.getName().equals(cookie.getName())
                    && java.util.Objects.equals(c.getDomain(), cookie.getDomain()));
            cookies.add(cookie);
        }
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        List<HttpCookie> result = new ArrayList<>();
        String host = uri.getHost();

        if (host == null) {
            return result;
        }

        synchronized (cookies) {
            for (HttpCookie cookie : cookies) {
                //System.out.println("  Checking cookie: name=" + cookie.getName() + ", domain='" + cookie.getDomain() + "'");
                String domain = cookie.getDomain();

                if (domain == null) {
                    // Host-only cookie — applies to the exact host that set it.
                    result.add(cookie);
                    continue;
                }

                // Normalize: cookies are often set with a leading dot (e.g. ".duckduckgo.com")
                // meaning "this domain AND all its subdomains". Strip the dot once, then
                // check both the exact-domain case and the subdomain case explicitly.
                String normalizedDomain = domain.startsWith(".") ? domain.substring(1) : domain;

                boolean isExactMatch = host.equalsIgnoreCase(normalizedDomain);
                boolean isSubdomainMatch = host.toLowerCase().endsWith("." + normalizedDomain.toLowerCase());

                if (isExactMatch || isSubdomainMatch) {
                    result.add(cookie);
                }
            }
        }
        return result;
    }

    @Override
    public List<HttpCookie> getCookies() {
        synchronized (cookies) {
            return new ArrayList<>(cookies);
        }
    }

    @Override
    public List<URI> getURIs() {
        return new ArrayList<>(); // not needed for this use case
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        synchronized (cookies) {
            return cookies.remove(cookie);
        }
    }

    @Override
    public boolean removeAll() {
        synchronized (cookies) {
            boolean hadAny = !cookies.isEmpty();
            cookies.clear();
            return hadAny;
        }
    }

    // A small serializable stand-in for HttpCookie, since HttpCookie itself
    // doesn't implement Serializable — we manually copy just the fields we need.
    private static class SerializableCookie implements Serializable {
        private final String name;
        private final String value;
        private final String domain;
        private final String path;
        private final long maxAge;
        private final boolean secure;

        SerializableCookie(HttpCookie cookie) {
            this.name = cookie.getName();
            this.value = cookie.getValue();
            this.domain = cookie.getDomain();
            this.path = cookie.getPath();
            this.maxAge = cookie.getMaxAge();
            this.secure = cookie.getSecure();
        }

        HttpCookie toHttpCookie() {
            HttpCookie cookie = new HttpCookie(name, value);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setMaxAge(maxAge);
            cookie.setSecure(secure);
            return cookie;
        }
    }
}