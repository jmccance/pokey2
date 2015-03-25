var babelify = require('babelify');
var browserify = require('browserify');
var del = require('del');
var eslint = require('gulp-eslint');
var gulp = require('gulp');
var rename = require('gulp-regex-rename');
var source = require('vinyl-source-stream');

var cwd = path.resolve('.');

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

gulp.task('lint', function () {
  return gulp.src(src.scripts)
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

gulp.task('compile', function () {
  return bundle();
});

gulp.task('default', ['clean', 'lint', 'compile']);

gulp.task('watch', ['default'], function() {
  gulp.watch(src.scripts, ['default']);
});
