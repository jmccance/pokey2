var babelify = require('babelify');
var browserify = require('browserify');
var gulp = require('gulp');
var del = require('del');
var reactify = require('reactify');
var rename = require('gulp-regex-rename');
var source = require('vinyl-source-stream');

var src = {
  scripts: 'js/**/*.js'
}

// Write output to the Play public directory.
var target = '../../public/dist'
var target = {
  scripts: target + '/js',
  lib: target + '/lib'
}

var bundler;
function getBundler() {
  if (!bundler) {
    bundler = browserify('./js/app.js', { debug: true });
  }
  return bundler;
}

function bundle() {
  return getBundler()
    .transform(babelify)
    .transform(reactify)
    .bundle()
    .on('error', function(err) { console.log('Error: ' + err.message); })
    .pipe(source('app.js'))
    .pipe(gulp.dest(target.scripts));
}

gulp.task('clean', function(cb) {
  del([
   target + '/*'
  ],
  { force: true },
  cb);
});

gulp.task('compile', function () {
  return bundle();
});

gulp.task('default', ['clean', 'compile']);

gulp.task('watch', function() {
  gulp.watch(src.scripts, ['default']);
});
