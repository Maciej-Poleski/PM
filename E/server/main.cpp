/*
 * Copyright (C) 2008 Emweb bvba, Heverlee, Belgium.
 *
 * See the LICENSE file for terms of use.
 */

#include <Wt/WApplication>
#include <Wt/WBreak>
#include <Wt/WContainerWidget>
#include <Wt/WLineEdit>
#include <Wt/WPushButton>
#include <Wt/WText>
#include <Wt/WEnvironment>
#include <Wt/WServer>
#include <Wt/WTextArea>

#include <Wt/Http/Client>
#include <Wt/Http/Message>

// c++0x only, for std::bind
#include <functional>

#include <boost/system/error_code.hpp>

using namespace Wt;

static std::string regId();
static void setRegId(const std::string &regId);

/*
 * A simple hello world application class which demonstrates how to react
 * to events, read input, and give feed-back.
 */
class HelloApplication : public WApplication
{
public:
    HelloApplication(const WEnvironment &env);

private:
    WLineEdit *_nameLineEdit;
    WTextArea *_messageBody;
    WText *greeting_;

    void greet();
    void sendMessage();
    void handleHttpResponse(boost::system::error_code err, const Http::Message &response);
};

/*
 * The env argument contains information about the new session, and
 * the initial request. It must be passed to the WApplication
 * constructor so it is typically also an argument for your custom
 * application constructor.
 */
HelloApplication::HelloApplication(const WEnvironment &env)
    : WApplication(env)
{
    std::cerr<<"-------------------------------------\n";
    for(const auto m : env.getParameterMap())
    {
        std::cerr<<"Klucz: "<<m.first<<'\n';
        for(const auto s : m.second)
        {
            std::cerr<<s<<'\n';
        }
    }
    
    setTitle("Hello world");                               // application title

    root()->addWidget(new WText("Name:"));  // show some text
    _nameLineEdit = new WLineEdit(root());                     // allow text input
    _nameLineEdit->setFocus();                                 // give focus

    root()->addWidget(new WBreak());

    _messageBody = new WTextArea("Message", root());

    root()->addWidget(new WBreak());

    WPushButton *button
    = new WPushButton("Send", root());              // create a button
    button->setMargin(10, Left | Top | Bottom);            // add 5 pixels margin

    root()->addWidget(new WBreak());                       // insert a line break

    greeting_ = new WText(root());                         // empty text

    /*
     * Connect signals with slots
     *
     * - simple Wt-way
     */
    button->clicked().connect(this, &HelloApplication::greet);

    /*
     * - using an arbitrary function object (binding values with boost::bind())
     */
    _messageBody->enterPressed().connect
    (std::bind(&HelloApplication::greet, this));

    /*
     * - using a c++0x lambda:
     */
    // b->clicked().connect(std::bind([=]() {
    //       greeting_->setText("Hello there, " + nameEdit_->text());
    // }));
}

void HelloApplication::greet()
{
    /*
     * Update the text, using text input into the nameEdit_ field.
     */
    sendMessage();
}

void HelloApplication::sendMessage()
{
    Http::Client *client = new Http::Client(this);
    client->setTimeout(15);
    client->setMaximumResponseSize(10 * 1024);
    client->done().connect(boost::bind(&HelloApplication::handleHttpResponse, this, _1, _2));
    Http::Message message;
    message.setHeader("Content-Type", "application/json");
    message.setHeader("Authorization", "key=AIzaSyAoynlFvcOvbo4QyNP7MryXeShekCWi3Sw");
    message.addBodyText("{\n"
                        "\"registration_ids\" : [\"" + regId() + "\"],\n"
                        "\"data\" : {\n"
                        "\"name\" : \"" + _nameLineEdit->text().toUTF8() + "\"\n"
                        "\"message\" : \"" + _messageBody->text().toUTF8() + "\"\n"
                        "},\n"
                        "}");
    if (client->post("https://android.googleapis.com/gcm/send", message)) {
        greeting_->setText("Sending...");
    } else {
        greeting_->setText("Error (POST message is not sent)");
    }
    enableUpdates();
}

void HelloApplication::handleHttpResponse(boost::system::error_code err, const Http::Message &response)
{
    if (!err) {
        greeting_->setText("HTTP RESPONSE: " + boost::lexical_cast<std::string>(response.status()) + "<br/>\n<br/>\n" + response.body());
        std::cerr << response.body() << '\n';
    } else {
        greeting_->setText("An error occured");
    }
    triggerUpdate();
}

#include <Wt/WResource>

class RegIdResource : public WResource
{
    virtual void handleRequest(const Http::Request &request, Http::Response &response);
};

#include <Wt/Http/Response>
#include <fstream>

void RegIdResource::handleRequest(const Http::Request &request, Http::Response &response)
{
    std::cerr<<"------------------REQ----------------\n";
    for(const auto m : request.getParameterMap())
    {
        std::cerr<<"Klucz: "<<m.first<<'\n';
        for(const auto s : m.second)
        {
            std::cerr<<s<<'\n';
        }
    }
    if (!request.getParameterValues("regId").empty())
        setRegId(request.getParameterValues("regId")[0]);
    std::ofstream("/tmp/req");
}

std::string regId()
{
    std::ifstream in("/tmp/regId");
    std::string result;
    in >> result;
    return result;
}

void setRegId(const std::string &regId)
{
    std::ofstream out("/tmp/regId");
    out << regId;
}

WApplication *createApplication(const WEnvironment &env)
{
    /*
     * You could read information from the environment to decide whether
     * the user has permission to start a new application
     */
    return new HelloApplication(env);
}

#include <thread>
#include <signal.h>

int MyRun(int argc, char *argv[], ApplicationCreator createApplication)
{
    try {
        // use argv[0] as the application name to match a suitable entry
        // in the Wt configuration file, and use the default configuration
        // file (which defaults to /etc/wt/wt_config.xml unless the environment
        // variable WT_CONFIG_XML is set)
        WServer server(argv[0]);
        
        // WTHTTP_CONFIGURATION is e.g. "/etc/wt/wthttpd"
        server.setServerConfiguration(argc, argv, WTHTTP_CONFIGURATION);
        
        // add a single entry point, at the default location (as determined
        // by the server configuration's deploy-path)
        server.addEntryPoint(Wt::Application, createApplication);
        server.addResource(new RegIdResource(),"/register/");
        //server.addResource(new RegIdResource(),"/");
        if (server.start()) {
            int sig = WServer::waitForShutdown(argv[0]);
            
            std::cerr << "Shutdown (signal = " << sig << ")" << std::endl;
            server.stop();
            
            if (sig == SIGHUP)
                WServer::restart(argc, argv, environ);
        }
    } catch (WServer::Exception& e) {
        std::cerr << e.what() << "\n";
        return 1;
    } catch (std::exception& e) {
        std::cerr << "exception: " << e.what() << "\n";
        return 1;
    }
}

int main(int argc, char **argv)
{
    /*
     * Your main method may set up some shared resources, but should then
     * start the server application (FastCGI or httpd) that starts listening
     * for requests, and handles all of the application life cycles.
     *
     * The last argument to WRun specifies the function that will instantiate
     * new application objects. That function is executed when a new user surfs
     * to the Wt application, and after the library has negotiated browser
     * support. The function should return a newly instantiated application
     * object.
     */
    return MyRun(argc, argv, &createApplication);
}



