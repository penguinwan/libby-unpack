# Chrome
1. go to libby
1. open developer tools
1. open borrowed book
1. export HAR
1. create dir, name it `root`
1. put HAR in `root`
1. put `unpack.groovy` a level above `root`
1. open `unpack.groovy`, change `root` and `bookHar`
1. run `groovy unpack.groovy`
1. check `root/toc.json`
1. check the content `images` `css` `Text` directories

# Calibre
1. at calibre
1. add book from a single folder >> select all.xhtml
1. select the book >> convert book individually
1. open Structure Detection
1. add Detect Chapter at //*[((name()='h1' or name()='h2' or name()='h3' or name()='h4'))]
1. add Insert page break before //*[name()='h1' or name()='h2' or name()='h3' or name()='h4']
1. open Look & feel
1. copy all css and paste at Styling >> Extra css